package controllers;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.larvalabs.redditchat.ChatCommands;
import com.larvalabs.redditchat.Constants;
import com.larvalabs.redditchat.dataobj.JsonChatRoom;
import com.larvalabs.redditchat.dataobj.JsonMessage;
import com.larvalabs.redditchat.dataobj.JsonUser;
import com.larvalabs.redditchat.realtime.ChatRoomStream;
import com.larvalabs.redditchat.util.Stats;
import com.larvalabs.redditchat.util.Util;
import jobs.SaveNewMessageJob;
import models.ChatRoom;
import models.ChatUser;
import models.ChatUserRoomJoin;
import play.Logger;
import play.Play;
import play.libs.F.EventStream;
import play.libs.F.Promise;
import play.mvc.Http.WebSocketClose;
import play.mvc.Http.WebSocketFrame;
import play.mvc.WebSocketController;
import play.mvc.With;

import java.util.*;

@With(ForceSSL.class)
public class WebSocket extends PreloadUserController {

    private static ChatUser getUser() {
        ChatUser user;
        if (session.contains("uid")) {
            String uid = session.get(Application.SESSION_UID);
            Logger.info("websocket found existing user: " + uid);
            return ChatUser.get(uid);
        } else {
            Logger.info("No user id in session.");
            return null;
        }
    }

    public static void room(String roomName){
        ChatUser user = connected();
        ChatRoom room = null;
        if (roomName != null) {
            room = ChatRoom.findOrCreateForName(roomName);
            if (!room.isOpen()) {
                Logger.info("Room "+roomName+" not open, directing to room wait page.");
                Application.roomWait(roomName, null);
                return;
            }
        }

        if (user == null) {
            user = ChatUser.findOrCreate(Constants.USERNAME_GUEST);
            try {
                user.joinChatRoom(room);
            } catch (ChatUser.UserBannedException e) {
                Logger.error("Preview user banned.");
            }
            setUserInSession(user);
        }

        List<ChatUserRoomJoin> chatRoomJoins = user.getChatRoomJoins();
        if (roomName == null || room == null) {
            if (chatRoomJoins.size() == 0) {
                room = ChatRoom.findByName(Constants.CHATROOM_DEFAULT);
                try {
                    user.joinChatRoom(room);
                } catch (ChatUser.UserBannedException e) {
                    // todo show message that they're banned
                    Application.index();
                }
            }
        } else {
            try {
                user.joinChatRoom(room);
            } catch (ChatUser.UserBannedException e) {
                // todo show message that they're banned
                Application.index();
            }
        }

        // Links to other suggested rooms
        List<ChatRoom> activeRooms = new ArrayList<ChatRoom>();
        {
            ChatRoom breakerapp = ChatRoom.findByName("breakerapp");
            if (!existsInJoins(chatRoomJoins, breakerapp)) {
                activeRooms.add(breakerapp);
            }
        }
        {
            ChatRoom breakerapp = ChatRoom.findByName("SideProject");
            if (!existsInJoins(chatRoomJoins, breakerapp)) {
                activeRooms.add(breakerapp);
            }
        }
        {
            ChatRoom breakerapp = ChatRoom.findByName("webdev");
            if (!existsInJoins(chatRoomJoins, breakerapp)) {
                activeRooms.add(breakerapp);
            }
        }

        String userString = new Gson().toJson(JsonUser.fromUser(user));
        String environment = Play.mode.isProd() ? "production" : "dev";

        render("index.html", user, userString, roomName, environment);
    }

    public static void roomOld(String roomName) {
        ChatUser user = connected();
        ChatRoom room = null;
        if (roomName != null) {
            room = ChatRoom.findOrCreateForName(roomName);
            if (!room.isOpen()) {
                Logger.info("Room "+roomName+" not open, directing to room wait page.");
                Application.roomWait(roomName, null);
                return;
            }
        }

        if (user == null) {
            user = ChatUser.findOrCreate(Constants.USERNAME_GUEST);
            try {
                user.joinChatRoom(room);
            } catch (ChatUser.UserBannedException e) {
                Logger.error("Preview user banned.");
            }
            setUserInSession(user);
        }

        List<ChatUserRoomJoin> chatRoomJoins = user.getChatRoomJoins();
        if (roomName == null || room == null) {
            if (chatRoomJoins.size() == 0) {
                room = ChatRoom.findOrCreateForName(Constants.CHATROOM_DEFAULT);
                try {
                    user.joinChatRoom(room);
                } catch (ChatUser.UserBannedException e) {
                    // todo show message that they're banned
                    Application.index();
                }
            }
        } else {
            try {
                user.joinChatRoom(room);
            } catch (ChatUser.UserBannedException e) {
                // todo show message that they're banned
                Application.index();
            }
        }

        // Links to other suggested rooms
        List<ChatRoom> activeRooms = new ArrayList<ChatRoom>();
        {
            ChatRoom breakerapp = ChatRoom.findOrCreateForName("breakerapp");
            if (!existsInJoins(chatRoomJoins, breakerapp)) {
                activeRooms.add(breakerapp);
            }
        }
        {
            ChatRoom breakerapp = ChatRoom.findOrCreateForName("SideProject");
            if (!existsInJoins(chatRoomJoins, breakerapp)) {
                activeRooms.add(breakerapp);
            }
        }
        {
            ChatRoom breakerapp = ChatRoom.findOrCreateForName("webdev");
            if (!existsInJoins(chatRoomJoins, breakerapp)) {
                activeRooms.add(breakerapp);
            }
        }

        render("WebSocket/room3.html", user, roomName, activeRooms);
    }

    private static boolean existsInJoins(List<ChatUserRoomJoin> joins, ChatRoom room) {
        for (ChatUserRoomJoin join : joins) {
            if (join.room.equals(room)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Just used to wake up the server after restart since the websocket controller
     * doesn't seem to count
     */
    public static void wakeup() {
        renderText("ok");
    }

    private static class RoomConnection {
        public ChatRoom room;
        public ChatRoomStream chatRoomEventStream;
        public ChatRoomStream chatRoomMessageStream;
        public EventStream<ChatRoomStream.Event> eventStream;
        public EventStream<ChatRoomStream.Event> messageStream;
        public boolean isModerator;
        public boolean canPost;

        public RoomConnection(ChatUser user, ChatRoom room, ChatRoomStream chatRoomEventStream, ChatRoomStream chatRoomMessageStream, EventStream<ChatRoomStream.Event> eventStream, EventStream<ChatRoomStream.Event> messageStream, boolean isModerator) {
            this.room = room;
            this.chatRoomEventStream = chatRoomEventStream;
            this.chatRoomMessageStream = chatRoomMessageStream;
            this.eventStream = eventStream;
            this.messageStream = messageStream;
            this.isModerator = isModerator;
            this.canPost = room.userCanPost(user);
        }
    }

    public static class ChatRoomSocket extends WebSocketController {

        public static void join() {

            ChatUser user = getUser();
            if (user == null) {
                disconnect();
                return;
            }
            String connectionId = Util.getUUID();
            List<ChatUserRoomJoin> chatRoomJoins = user.getChatRoomJoins();

            HashMap<String, RoomConnection> roomConnections = new HashMap<java.lang.String, RoomConnection>();
            ArrayList<JsonChatRoom> jsonChatRoomsList = new ArrayList<JsonChatRoom>();

            {
                int i = 0;
                for (ChatUserRoomJoin chatRoomJoin : chatRoomJoins) {
                    ChatRoom room = chatRoomJoin.getRoom();
                    if (Constants.CHATROOM_DEFAULT.equals(room.name) || room.isOpen()) {
                        jsonChatRoomsList.add(JsonChatRoom.from(room, user));
                        addConnection(user, connectionId, roomConnections, room);
                        Logger.debug("Connecting to chat room stream for " + room.getName()+", canpost "+room.userCanPost(user));
                        i++;
                    }
                }

                JsonChatRoom[] jsonChatRooms = jsonChatRoomsList.toArray(new JsonChatRoom[]{});

                Arrays.sort(jsonChatRooms, new Comparator<JsonChatRoom>() {
                    @Override
                    public int compare(JsonChatRoom o1, JsonChatRoom o2) {
                        return o1.name.compareTo(o2.name);
                    }
                });
                String roomListJson = new ChatRoomStream.RoomList(jsonChatRooms).toJson();
                Logger.info("Room list json: " + roomListJson);
                outbound.send(roomListJson);
            }

            Stats.count(Stats.StatKey.WEBSOCKET_CONNECT, 1);

            // Loop while the socket is open
            while (inbound.isOpen()) {

                awaitAndProcessInput(user, connectionId, roomConnections);

            }

        }

        private static void awaitAndProcessInput(ChatUser user, String connectionId, HashMap<String, RoomConnection> roomConnections) {
            Promise[] roomEventPromises = new Promise[(roomConnections.size() * 2) + 1];
            roomEventPromises[0] = inbound.nextEvent();
            int i = 1;
            for (RoomConnection roomConnection : roomConnections.values()) {
                roomEventPromises[i] = roomConnection.eventStream.nextEvent();
                i++;
                roomEventPromises[i] = roomConnection.messageStream.nextEvent();
                i++;
            }

            Object awaitResult = await(Promise.waitAny(roomEventPromises));

            // Case: TextEvent received on the socket
            if (awaitResult instanceof WebSocketFrame) {
                WebSocketFrame frame = (WebSocketFrame) awaitResult;
                if (!frame.isBinary && frame.textData != null) {
                    String userMessageJson = frame.textData;

                    try {
                        Logger.debug("Message received from socket (" + user.username + "): " + userMessageJson);
                        JsonObject msgObj = new JsonParser().parse(userMessageJson).getAsJsonObject();
                        String roomName = msgObj.get("roomName").getAsString();
                        String message = Util.cleanAndLimitLength(msgObj.get("message").getAsString(), Constants.MAX_MSG_LENGTH);
                        if (message == null || message.length() == 0) {
                            Logger.debug("After cleaning message length was 0, dropping.");
                            return;
                        }
                        RoomConnection roomConnection = roomConnections.get(roomName);
                        if (roomConnection != null) {
                            if (message.toLowerCase().equals("##ping##")) {
                                for (RoomConnection connection : roomConnections.values()) {
                                    connection.room.userPresent(user, connectionId);
                                }
                                // todo also add to global users connected set for easier stats
                                //                        Logger.debug("Ping msg - skipping.");
                            } else if (message.toLowerCase().equals("##memberlist##")) {
                                Logger.debug("User " + user.username + " requested member list.");

                                outbound.send(new ChatRoomStream.MemberList(JsonChatRoom.from(roomConnection.room, user),
                                        roomConnection.room.getAllUsersWithOnlineStatus()).toJson());
                            } else if (message.toLowerCase().equals("##markmessagesread##")) {
                                Logger.debug("User " + user.username + " marking messages read for " + roomName);

                                roomConnection.room.markMessagesReadForUser(user);
                            } else if (ChatCommands.isCommand(message)) {
                                try {
                                    ChatCommands.execCommand(user, roomConnection.room, message, roomConnection.chatRoomEventStream, outbound);
                                } catch (ChatCommands.NotEnoughPermissionsException e) {
                                    sendLocalServerMessage(roomConnection, "You don't have permission to execute this command.");
                                } catch (ChatCommands.CommandNotRecognizedException e) {
                                    sendLocalServerMessage(roomConnection, "Command not recognized.");
                                }
                            } else {
                                if (roomConnection.canPost) {
                                    String uuid = Util.getUUID();
                                    JsonMessage jsonMessage = JsonMessage.makePresavedMessage(uuid, user, roomConnection.room, message);
                                    new SaveNewMessageJob(uuid, user, roomName, message).now();
                                    roomConnection.chatRoomMessageStream.say(jsonMessage);
                                    Stats.count(Stats.StatKey.MESSAGE, 1);
                                } else {
                                    Logger.info("User " + user.getUsername() + " cannot post to " + roomName);
                                    // Direct message to user who tried to send this
                                    sendLocalServerMessage(roomConnection, "You cannot post to this room.");
                                }
                            }
                        } else {
                            Logger.error("Could not find room connection.");
                        }
                    } catch (Exception e1) {
                        Logger.error(e1, "Error handling user message, discarding.");
                    }
                }

            } else if (awaitResult instanceof WebSocketClose) {
                // Case: The socket has been closed
                Logger.info("Socket closed: " + user.getUsername() + ":" + connectionId);
                for (RoomConnection roomConnection : roomConnections.values()) {
                    roomConnection.room.userNotPresent(user, connectionId);
                    // If this was the last connection that user had to the room then broadcast they've left
                    if (!roomConnection.room.isUserPresent(user)) {
                        Logger.debug("Last connection for " + user.username + " on channel " + roomConnection.room.getName() + " disconnected, broadcasting leave.");
                        roomConnection.chatRoomEventStream.leave(roomConnection.room, user);
                    }
                }
                disconnect();

            } else if (awaitResult instanceof ChatRoomStream.Event) {

                if (awaitResult instanceof ChatRoomStream.ServerCommand) {
                    // Case: A command affecting users
                    ChatRoomStream.ServerCommand commandEvent = (ChatRoomStream.ServerCommand) awaitResult;
                    if (commandEvent.command.username != null && commandEvent.command.username.equals(user.username)) {
                        Logger.info("Received " + commandEvent.command.type + " for this user.");
                        if (commandEvent.command.type.shouldCloseClientSocket()) {
                            Logger.info(user.username + " has been disconnected from " + commandEvent.room.name);

                            RoomConnection roomConnection = roomConnections.get(commandEvent.room.name);
                            user.leaveChatRoom(roomConnection.room);
                            roomConnections.remove(commandEvent.room.name);

                            sendLocalServerMessage(roomConnection, commandEvent.command.username + " was kicked.");
                        }
                    }
                } else {
                    // Case: New message on a chat room
                    ChatRoomStream.Event event = (ChatRoomStream.Event) awaitResult;
                    String json = event.toJson();
//                    Logger.debug("Sending event to " + user.username + ":" + connectionId + " - " + json);
                    outbound.send(json);
                }
            }
        }

        private static void sendLocalServerMessage(RoomConnection roomConnection, String message) {
            outbound.send(new ChatRoomStream.ServerMessage(JsonChatRoom.from(roomConnection.room), message).toJson());
        }

        private static void addConnection(ChatUser user, String connectionId, HashMap<String, RoomConnection> roomConnections, ChatRoom room) {
            ChatRoomStream eventStream = ChatRoomStream.getEventStream(room.name);
            ChatRoomStream messageStream = ChatRoomStream.getMessageStream(room.name);

            // Socket connected, join the chat room
            // If this is the first connection this user has to the room then broadcast
            boolean broadcastJoin = !room.isUserPresent(user);
            if (broadcastJoin) {
                Logger.debug("First connection for " + user.username + ", broadcasting join for connectionId " + connectionId);
            }
            room.userPresent(user, connectionId);
            boolean isModerator = room.isModerator(user);
            EventStream<ChatRoomStream.Event> roomEventsStream = eventStream.join(room, user, broadcastJoin);
            EventStream<ChatRoomStream.Event> roomMessagesStream = messageStream.join(room, user, broadcastJoin);

            roomConnections.put(room.name, new RoomConnection(user, room, eventStream, messageStream, roomMessagesStream, roomEventsStream, isModerator));
        }

    }

}

