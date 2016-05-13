package com.larvalabs.redditchat.realtime;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.larvalabs.redditchat.ChatCommands;
import com.larvalabs.redditchat.Constants;
import com.larvalabs.redditchat.dataobj.JsonChatRoom;
import com.larvalabs.redditchat.dataobj.JsonMessage;
import com.larvalabs.redditchat.dataobj.JsonUser;
import com.larvalabs.redditchat.util.Stats;
import com.larvalabs.redditchat.util.Util;
import controllers.WebSocket;
import jobs.RedisQueueJob;
import models.ChatRoom;
import models.ChatUser;
import play.Logger;
import play.libs.F.EventStream;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

public class ChatRoomStream {

    public static final int STREAM_SIZE = 0;
    public static final int PRELOAD_NUM_MSGS_ON_STARTUP = STREAM_SIZE;

    private ConcurrentHashMap<String, EventStream<Event>> userStreams = new ConcurrentHashMap<>();

    private String name;

    public ChatRoomStream(String name, boolean isMessageStream) {
        this.name = name;
    }

/*
    private void loadOldMessages() {
        Logger.debug("Loading old messages for room: " + name);
        ChatRoom room = ChatRoom.findByName(name);
        List<models.Message> messages = room.getMessages(PRELOAD_NUM_MSGS_ON_STARTUP);
        // We're pushing onto a queue here so go in sqeuential order
        Collections.reverse(messages);
        for (models.Message message : messages) {
            chatEvents.publish(new Message(JsonMessage.from(message, message.getUser().getUsername(), name), JsonChatRoom.from(room), JsonUser.fromUser(message.getUser())));
        }
        Logger.debug("Old messages sent to room: " + name);
    }
*/

    public String getName() {
        return name;
    }

    // ~~~~~~~~~ Let's chat!

    private String getStreamKey(String roomName, String username, String connectionId) {
        return roomName + "-" + username + "-" + connectionId;
    }

    /**
     * For WebSocket, when a user join the room we return a continuous event stream
     * of ChatEvent
     */
    public EventStream<ChatRoomStream.Event> join(ChatRoom room, ChatUser user, String connectionId, boolean broadcastJoin) {
        if (broadcastJoin) {
            JsonUser jsonUser = JsonUser.fromUser(user, true);
            publishEvent(new Join(JsonChatRoom.from(room, room.getModeratorUsernames()), jsonUser));
        }
        String streamKey = getStreamKey(room.getName(), user.getUsername(), connectionId);
        EventStream<Event> userEventStream = userStreams.get(streamKey);
        if (userEventStream == null) {
            userEventStream = new EventStream<Event>();
            userStreams.put(streamKey, userEventStream);
        }
        if (room.isDefaultRoom()) {
            Stats.sample(Stats.StatKey.USER_STREAMS_OPEN, userStreams.size());
        }
        return userEventStream;
    }

    public void sendMemberList(ChatRoom room) {
//        String[] usernames = room.getUsernamesPresent().toArray(new String[]{});
//        TreeSet<ChatUser> users = room.getPresentUserObjects();
//        Logger.info("Sending member list of length " + users.size());
        publishEvent(new MemberList(JsonChatRoom.from(room, room.getModeratorUsernames()), room.getAllUsersWithOnlineStatus()));
    }

    /**
     * A user leave the room
     */
    public void leave(JsonChatRoom room, JsonUser user, String connectionId) {
        // todo Review whether this can leak, i.e. if we don't always get proper disconnect events from the socket
        // todo Could do a periodic cleanup where we remove streams that haven't been accessed in a while
        userStreams.remove(getStreamKey(room.name, user.username, connectionId));
        if (room.isDefaultRoom()) {
            Stats.sample(Stats.StatKey.USER_STREAMS_OPEN, userStreams.size());
        }
        user.online = false;
        publishEvent(new Leave(room, user));
    }

    /**
     * A user say something on the room
     */
    public void say(JsonMessage message, JsonChatRoom room, JsonUser user) {
        // todo maybe move this empty check elsewhere?
        if (message.message == null || message.message.trim().equals("")) {
            return;
        }
        publishEvent(new Message(message, room, user));
    }

    /**
     * A transient message (doesn't get saved to database or cache) from the server to a specific user
     * @param room
     * @param toUsername
     * @param message
     */
    public void sayFromServer(JsonChatRoom room, String toUsername, String message) {
        ServerMessage serverMessage = new ServerMessage(room, toUsername, JsonUser.fromUser(ChatUser.getSystemUser(), true), message);
        publishEvent(serverMessage);
    }

    public void sendMessageUpdate(ChatRoom room, models.Message message) {
        JsonMessage jsonMessage = JsonMessage.from(message, message.getUser().getUsername(), room.getName());
        jsonMessage.setLinkInfo(message.getLinks());
        JsonChatRoom jsonRoom = JsonChatRoom.from(room, room.getModeratorUsernames());
        publishEvent(new UpdateMessageEvent(jsonRoom, jsonMessage));
    }

    public void sendUserUpdate(ChatRoom room, ChatUser user, boolean isOnline) {
        JsonUser jsonUser = JsonUser.fromUser(user, isOnline);
        JsonChatRoom jsonRoom = JsonChatRoom.from(room, room.getModeratorUsernames());
        publishEvent(new UpdateUserEvent(jsonRoom, jsonUser));
    }

    public void sendRoomUpdate(ChatRoom room) {
        publishEvent(new UpdateRoomEvent(JsonChatRoom.from(room, room.getModeratorUsernames())));
    }

    public void publishEvent(Event event) {
        RedisQueueJob.publish(event);
    }

    public void publishEventInternal(Event event) {
        for (EventStream<Event> userEventStream : userStreams.values()) {
            userEventStream.publish(event);
        }
    }

    /**
     * For long polling, as we are sometimes disconnected, we need to pass 
     * the last event seen id, to be sure to not miss any message
     */
/*
    public Promise<List<IndexedEvent<ChatRoomStream.Event>>> nextMessages(long lastReceived) {
        return chatEvents.nextEvents(lastReceived);
    }
*/

    /**
     * For active refresh, we need to retrieve the whole message archive at
     * each refresh
     */
/*
    public List<ChatRoomStream.Event> archive() {
        return chatEvents.archive();
    }
*/

    // ~~~~~~~~~ Chat room events

    public static abstract class Event {

        public String type;
        public Long timestamp;
        public JsonChatRoom room;
        public String toUsername;

        public static final String TYPE_ROOMLIST = "roomlist";
        public static final String TYPE_MEMBERLIST = "memberlist";
        public static final String TYPE_MARKEDREAD = "markedread";
        public static final String TYPE_JOIN = "join";
        public static final String TYPE_MESSAGE = "message";
        public static final String TYPE_SERVERMESSAGE = "servermessage";
        public static final String TYPE_SERVERCOMMAND = "servercommand";
        public static final String TYPE_LEAVE = "leave";
        public static final String TYPE_UPDATE_USER = "updateuser";
        public static final String TYPE_UPDATE_ROOM = "updateroom";
        public static final String TYPE_UPDATE_MESSAGE = "updatemessage";

        public Event() {
        }

        public Event(String type, JsonChatRoom room) {
            this.type = type;
            this.room = room;
            this.timestamp = System.currentTimeMillis();
        }

        public Event(String type, JsonChatRoom room, String toUsername) {
            this(type, room);
            this.toUsername = toUsername;
        }

        public String toJson() {
            String json = new Gson().toJson(this);
            return json;
        }

        public static Event fromJson(String jsonStr) {
            JsonObject obj = new JsonParser().parse(jsonStr).getAsJsonObject();
            String type = obj.get("type").getAsString();
            Gson gson = new Gson();
            if (type.equals(TYPE_MEMBERLIST)) {
                return gson.fromJson(jsonStr, MemberList.class);
            } else if (type.equals(TYPE_MARKEDREAD)) {
                return gson.fromJson(jsonStr, MarkedRead.class);
            } else if (type.equals(TYPE_JOIN)) {
                return gson.fromJson(jsonStr, Join.class);
            } else if (type.equals(TYPE_MESSAGE)) {
                return gson.fromJson(jsonStr, Message.class);
            } else if (type.equals(TYPE_LEAVE)) {
                return gson.fromJson(jsonStr, Leave.class);
            } else if (type.equals(TYPE_SERVERMESSAGE)) {
                // Note these server commands are normally only sent locally to the websocket
                return gson.fromJson(jsonStr, ServerMessage.class);
            } else if (type.equals(TYPE_SERVERCOMMAND)) {
                return gson.fromJson(jsonStr, ServerCommand.class);
            } else if (type.equals(TYPE_UPDATE_USER)) {
                return gson.fromJson(jsonStr, UpdateUserEvent.class);
            } else if (type.equals(TYPE_UPDATE_ROOM)) {
                return gson.fromJson(jsonStr, UpdateRoomEvent.class);
            } else if (type.equals(TYPE_UPDATE_MESSAGE)) {
                return gson.fromJson(jsonStr, UpdateMessageEvent.class);
            }
            Logger.error("Event not recognized.");
            return null;
        }
    }

    public static class RoomList extends Event {

        public JsonChatRoom[] rooms;

        public RoomList() {
        }

        public RoomList(JsonChatRoom[] rooms) {
            super(TYPE_ROOMLIST, null);
            this.rooms = rooms;
        }
    }

    public static class MemberList extends Event {

        public JsonUser[] users;

        public MemberList() {
        }

        public MemberList(JsonChatRoom room, JsonUser[] users) {
            super(TYPE_MEMBERLIST, room);
            this.users = users;
        }
    }

    public static class MarkedRead extends Event {

        public long lastReadTime;

        public MarkedRead() {
        }

        public MarkedRead(JsonChatRoom room, String forUsername, long lastReadTime) {
            super(TYPE_MARKEDREAD, room, forUsername);
            this.lastReadTime = lastReadTime;
        }
    }

    public static class Join extends Event {

        public JsonUser user;

        public Join() {
        }

        public Join(JsonChatRoom room, JsonUser user) {
            super(TYPE_JOIN, room);
            this.user = user;
        }

    }

    public static class Leave extends Event {

        public JsonUser user;

        public Leave() {
        }

        public Leave(JsonChatRoom room, JsonUser user) {
            super(TYPE_LEAVE, room);
            this.user = user;
        }

    }

    public static class Message extends Event {

        public JsonUser user;         // Just for convenience on the front end
        public JsonMessage message;

        public Message() {
        }

        public Message(JsonMessage message, JsonChatRoom room, JsonUser user) {
            super(TYPE_MESSAGE, room);
            this.message = message;
            this.user = user;
        }

    }

    public static class UpdateUserEvent extends Event {
        public JsonUser user;

        public UpdateUserEvent() {
        }

        public UpdateUserEvent(JsonChatRoom room, JsonUser user) {
            super(TYPE_UPDATE_USER, room);
            this.user = user;
        }
    }

    public static class UpdateRoomEvent extends Event {
        public UpdateRoomEvent() {
        }

        public UpdateRoomEvent(JsonChatRoom room) {
            super(TYPE_UPDATE_ROOM, room);
        }
    }

    public static class UpdateMessageEvent extends Event {
        public JsonMessage message;

        public UpdateMessageEvent() {
        }

        public UpdateMessageEvent(JsonChatRoom room, JsonMessage message) {
            super(TYPE_UPDATE_MESSAGE, room);
            this.message = message;
        }
    }

    public static class ServerMessage extends Message {
        public ServerMessage(JsonChatRoom room, String toUsername, JsonUser serverBotUser, String message) {
            super(JsonMessage.makePresavedMessage(Util.getUUID(), Constants.SYSTEM_USERNAME, room.name, "<i>" + message + "</i>"), room, serverBotUser);
            this.toUsername = toUsername;
        }
    }

    public static class ServerCommand extends Event {
        public ChatCommands.Command command;

        public ServerCommand(JsonChatRoom room, ChatCommands.Command command) {
            super(TYPE_SERVERCOMMAND, room);
            this.command = command;
        }
    }


    // ~~~~~~~~~ Chat room factory

    private static HashMap<String, ChatRoomStream> chatRoomStreamsForRoom = new HashMap<String, ChatRoomStream>();

    public static ChatRoomStream getEventStream(String name) {
        synchronized (chatRoomStreamsForRoom) {
            ChatRoomStream chatRoomStream = chatRoomStreamsForRoom.get(name);
            if (chatRoomStream == null) {
                chatRoomStream = new ChatRoomStream(name, false);
                chatRoomStreamsForRoom.put(name, chatRoomStream);
            }
            return chatRoomStream;
        }
    }
}

