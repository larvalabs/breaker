package controllers;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.larvalabs.redditchat.ChatCommands;
import com.larvalabs.redditchat.Constants;
import com.larvalabs.redditchat.dataobj.JsonChatRoom;
import com.larvalabs.redditchat.dataobj.JsonMessage;
import com.larvalabs.redditchat.realtime.ChatRoomStream;
import com.larvalabs.redditchat.util.Util;
import jobs.SaveNewMessageJob;
import models.ChatRoom;
import models.ChatUser;
import models.ChatUserRoomJoin;
import play.Logger;
import play.libs.F.EventStream;
import play.libs.F.Promise;
import play.mvc.Controller;
import play.mvc.Http.WebSocketClose;
import play.mvc.Http.WebSocketFrame;
import play.mvc.WebSocketController;
import play.mvc.With;

import java.util.*;

@With(ForceSSL.class)
public class WebSocket extends Controller {

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

    public static void room(String roomName) {
        ChatUser user = getUser();
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
            Application.preAuthForRoomJoin(roomName);
            return;
        }
        if (roomName == null || room == null) {
            if (user.getChatRoomJoins().size() == 0) {
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
        render("WebSocket/room3.html", user, roomName);
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
        public ChatRoomStream roomStream;
        public EventStream<ChatRoomStream.Event> eventStream;
        public boolean canPost;

        public RoomConnection(ChatUser user, ChatRoom room, ChatRoomStream roomStream, EventStream<ChatRoomStream.Event> eventStream) {
            this.room = room;
            this.roomStream = roomStream;
            this.eventStream = eventStream;
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
                    if (room.isOpen()) {
                        jsonChatRoomsList.add(JsonChatRoom.from(room));
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
//            ChatRoom room = ChatRoom.findByName(roomName);
//            ChatRoomStream roomStream = ChatRoomStream.get(roomName);

            // Loop while the socket is open
            while (inbound.isOpen()) {

                awaitAndProcessInput(user, connectionId, roomConnections);

            }

        }

        private static void awaitAndProcessInput(ChatUser user, String connectionId, HashMap<String, RoomConnection> roomConnections) {
            Promise[] roomEventPromises = new Promise[roomConnections.size() + 1];
            roomEventPromises[0] = inbound.nextEvent();
            int i = 1;
            for (RoomConnection roomConnection : roomConnections.values()) {
                roomEventPromises[i] = roomConnection.eventStream.nextEvent();
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
                                roomConnection.room.userPresent(user, connectionId);
                                //                        Logger.debug("Ping msg - skipping.");
                            } else if (message.toLowerCase().equals("##memberlist##")) {
                                Logger.debug("User " + user.username + " requested member list.");
                                outbound.send(new ChatRoomStream.MemberList(JsonChatRoom.from(roomConnection.room, user),
                                        roomConnection.room.getPresentJsonUsers()).toJson());
                            } else if (ChatCommands.isCommand(message)) {
                                ChatCommands.execCommand(roomConnection.room, message, roomConnection.roomStream, outbound);
                            } else {
                                if (roomConnection.canPost) {
                                    String uuid = Util.getUUID();
                                    JsonMessage jsonMessage = JsonMessage.makePresavedMessage(uuid, user, roomConnection.room, message);
                                    new SaveNewMessageJob(uuid, user, roomName, message).now();
                                    roomConnection.roomStream.say(jsonMessage);
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
                        roomConnection.roomStream.leave(roomConnection.room, user);
                    }
                }
                disconnect();

            } else if (awaitResult instanceof ChatRoomStream.Event) {

                if (awaitResult instanceof ChatRoomStream.ServerCommand) {
                    // Case: A command affecting users
                    ChatRoomStream.ServerCommand commandEvent = (ChatRoomStream.ServerCommand) awaitResult;
                    if (commandEvent.command.username != null && commandEvent.command.username.equals(user.username)) {
                        Logger.info("Received " + commandEvent.command.type + " for this user.");
                        if (commandEvent.command.type == ChatCommands.CommandType.KICK) {
                            Logger.info(user.username + " has been kicked from " + commandEvent.room.name);

                            RoomConnection roomConnection = roomConnections.get(commandEvent.room.name);
                            user.leaveChatRoom(roomConnection.room);
                            roomConnections.remove(commandEvent.room.name);
                            // todo send event to UI

                            sendLocalServerMessage(roomConnection, commandEvent.command.username + " was kicked.");
                        }
                    }
                } else {
                    // Case: New message on a chat room
                    ChatRoomStream.Event event = (ChatRoomStream.Event) awaitResult;
                    String json = event.toJson();
                    Logger.debug("Sending event to " + user.username + ":" + connectionId + " - " + json);
                    outbound.send(json);
                }
            }
        }

        private static void sendLocalServerMessage(RoomConnection roomConnection, String message) {
            outbound.send(new ChatRoomStream.ServerMessage(JsonChatRoom.from(roomConnection.room), message).toJson());
        }

        private static void addConnection(ChatUser user, String connectionId, HashMap<String, RoomConnection> roomConnections, ChatRoom room) {
            ChatRoomStream roomStream = ChatRoomStream.get(room.name);

            // Socket connected, join the chat room
            // If this is the first connection this user has to the room then broadcast
            boolean broadcastJoin = !room.isUserPresent(user);
            if (broadcastJoin) {
                Logger.debug("First connection for " + user.username + ", broadcasting join for connectionId " + connectionId);
            }
            room.userPresent(user, connectionId);
            EventStream<ChatRoomStream.Event> roomMessagesStream = roomStream.join(room, user, broadcastJoin);

            roomConnections.put(room.name, new RoomConnection(user, room, roomStream, roomMessagesStream));
        }

    }

}

