package controllers;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.larvalabs.redditchat.ChatCommands;
import com.larvalabs.redditchat.Constants;
import com.larvalabs.redditchat.dataobj.*;
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

import static controllers.Application.SESSION_JOINROOM;

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
        long startTime = System.currentTimeMillis();

        ChatUser user = connected();
        ChatRoom room = null;
        if (roomName != null) {
            room = ChatRoom.findOrCreateForName(roomName);
            if (!room.isOpen()) {
                Logger.info("Room "+roomName+" not open, directing to room wait page.");
                Application.roomWait(roomName, null);
                return;
            }
        } else {
            redirect("/c/" + Constants.CHATROOM_DEFAULT);
            return;
        }

        if (user == null) {
            if (room.isPrivateRoom()) {
                Logger.info("Guest user can't see private room, showing login required screen.");
                session.put(SESSION_JOINROOM, roomName);
                render("WebSocket/privateRoomGuest.html", room);
                return;
            }
            user = ChatUser.findOrCreate(Constants.USERNAME_GUEST);
            try {
                user.joinChatRoom(room);
            } catch (ChatUser.UserBannedException e) {
                Logger.error("Preview user banned.");
            } catch (ChatUser.UnableToCheckAccessToPrivateRoom unableToCheckAccessToPrivateRoom) {
                // Should never happen because we bail above
                unableToCheckAccessToPrivateRoom.printStackTrace();
            } catch (ChatUser.NoAccessToPrivateRoomException e) {
                // Should never happen because we bail above
                e.printStackTrace();
            }
            setUserInSession(user);
        }

        if (roomName == null || room == null) {
            List<ChatUserRoomJoin> chatRoomJoins = user.getChatRoomJoins();
            if (chatRoomJoins.size() == 0) {
                room = ChatRoom.findByName(Constants.CHATROOM_DEFAULT);
                try {
                    user.joinChatRoom(room);
                } catch (ChatUser.UserBannedException e) {
                    // todo show message that they're banned
                    Application.index();
                } catch (ChatUser.UnableToCheckAccessToPrivateRoom unableToCheckAccessToPrivateRoom) {
                    // not gonna happen
                } catch (ChatUser.NoAccessToPrivateRoomException e) {
                    // not gonna happen
                }
            }
        } else {
            try {
                user.joinChatRoom(room);
            } catch (ChatUser.UserBannedException e) {
                // todo show message that they're banned
                Application.index();
            } catch (ChatUser.UnableToCheckAccessToPrivateRoom unableToCheckAccessToPrivateRoom) {
                String errorMessage = "We are having a temporary problem verifying your access to this room, please try again later. (Usually this is a temporary problem contacting Reddit).";
                render("WebSocket/privateRoomError.html", room, errorMessage);
                return;
            } catch (ChatUser.NoAccessToPrivateRoomException e) {
                String errorMessage = "You do not have permission to access this room.";
                render("WebSocket/privateRoomError.html", room, errorMessage);
                return;
            }
        }

        Gson gson = new Gson();

        JsonUtil.FullState fullState = JsonUtil.loadFullStateForUser(user);

        TreeMap<String, JsonChatRoom> rooms = fullState.rooms;
        TreeMap<String, JsonUser> allUsers = fullState.allUsers;
        TreeMap<String, JsonRoomMembers> members = fullState.members;
        TreeMap<String, ArrayList<String>> roomMessages = fullState.roomMessages;
        Map<String, JsonMessage> messages = fullState.messages;

        Logger.info("Websocket join time checkpoint post preload all state " + user.getUsername() + ": " + (System.currentTimeMillis() - startTime));

//        Logger.info("Websocket join time checkpoint 1 for " + user.getUsername() + ": " + (System.currentTimeMillis() - startTime));
        String roomsString = gson.toJson(rooms);
        String usersString = gson.toJson(allUsers);
        String membersString = gson.toJson(members);
        String roomMessagesString = gson.toJson(roomMessages);
        String messagesString = gson.toJson(messages);
        long loadTime = System.currentTimeMillis() - startTime;
        Logger.info("Websocket join time checkpoint 2 (post gson) for " + user.getUsername() + ": " + loadTime);
        Stats.measure(Stats.StatKey.INITIALPAGE_TIME, loadTime);

        // Links to other suggested rooms
        List<ChatRoom> activeRooms = new ArrayList<ChatRoom>();
/*
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
*/

        String userString = gson.toJson(JsonUser.fromUser(user, true));
        String environment = Play.mode.isProd() ? "production" : "dev";

        Logger.info("Websocket join time for " + user.getUsername() + ": " + (System.currentTimeMillis() - startTime));

        render("index.html", user, rooms, userString, roomName, environment, roomsString, usersString, membersString, roomMessagesString, messagesString);
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
        public JsonChatRoom room;
        public ChatRoomStream chatRoomEventStream;
        public EventStream<ChatRoomStream.Event> eventStream;
        public boolean isModerator;
        public boolean canPost;

        public RoomConnection(JsonChatRoom room, ChatRoomStream chatRoomEventStream, EventStream<ChatRoomStream.Event> eventStream, boolean isModerator, boolean canPost) {
            this.room = room;
            this.chatRoomEventStream = chatRoomEventStream;
            this.eventStream = eventStream;
            this.isModerator = isModerator;
            this.canPost = canPost;
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

            // This is a single element array because the current json user can get updated by events in an inner loop
            JsonUser[] jsonUser = new JsonUser[]{JsonUser.fromUser(user, true)};

            {
                int i = 0;
                for (ChatUserRoomJoin chatRoomJoin : chatRoomJoins) {
                    ChatRoom room = chatRoomJoin.getRoom();
                    if (Constants.CHATROOM_DEFAULT.equals(room.name) || room.isOpen()) {
                        jsonChatRoomsList.add(JsonChatRoom.from(room, room.getModeratorUsernames()));
                        addConnection(user, jsonUser[0], connectionId, roomConnections, room);
                        Logger.debug("Connecting to chat room stream for " + room.getName()+", canpost "+room.userCanPost(user));
                        i++;
                    }
                }
            }

            Stats.count(Stats.StatKey.WEBSOCKET_CONNECT, 1);

            // Loop while the socket is open
            while (inbound.isOpen()) {
                awaitAndProcessInput(jsonUser, connectionId, roomConnections);
            }

        }

        private static void awaitAndProcessInput(JsonUser[] singleUserArray, String connectionId, HashMap<String, RoomConnection> roomConnections) {
            JsonUser user = singleUserArray[0];
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
                processWebsocketFrame(user, connectionId, roomConnections, (WebSocketFrame) awaitResult);
            } else if (awaitResult instanceof WebSocketClose) {
                processWebsocketClose(user, connectionId, roomConnections);
            } else if (awaitResult instanceof ChatRoomStream.Event) {
                processEvent(singleUserArray, roomConnections, (ChatRoomStream.Event) awaitResult);
            }
        }

        private static void processEvent(JsonUser[] userArray, HashMap<String, RoomConnection> roomConnections, ChatRoomStream.Event awaitResult) {
            JsonUser user = userArray[0];
            if (awaitResult instanceof ChatRoomStream.ServerCommand) {
                // Case: A command affecting users
                ChatRoomStream.ServerCommand commandEvent = (ChatRoomStream.ServerCommand) awaitResult;
                if (commandEvent.command.username != null && commandEvent.command.username.equals(user.username)) {
                    Logger.info("Received " + commandEvent.command.type + " for this user.");
                    if (commandEvent.command.type.shouldCloseClientSocket()) {
                        Logger.info(user.username + " has been disconnected from " + commandEvent.room.name);

                        RoomConnection roomConnection = roomConnections.get(commandEvent.room.name);
                        ChatRoom roomModel = roomConnection.room.loadModelFromDatabase();
                        ChatUser userModel = user.loadModelFromDatabase();

                        userModel.leaveChatRoom(roomModel);
                        roomConnections.remove(commandEvent.room.name);

                        sendLocalServerMessage(roomConnection, commandEvent.command.username + " was kicked.");
                        disconnect();
                    }
                }
            } else if (awaitResult instanceof ChatRoomStream.UpdateUserEvent) {
                ChatRoomStream.UpdateUserEvent updateEvent = (ChatRoomStream.UpdateUserEvent) awaitResult;
                if (updateEvent.user.equals(user)) {
                    Logger.info("Updated local user object from event: " + updateEvent.user.username);
                    userArray[0] = updateEvent.user;
                }
                outbound.send(updateEvent.toJson());
            } else if (awaitResult instanceof ChatRoomStream.UpdateMessageEvent) {
                ChatRoomStream.UpdateMessageEvent updateEvent = (ChatRoomStream.UpdateMessageEvent) awaitResult;
                if (roomConnections.containsKey(updateEvent.room.name)) {
                    Logger.info("Updated local room object from event: " + updateEvent.room.name);
                    roomConnections.get(updateEvent.room.name).room = updateEvent.room;
                }
                outbound.send(updateEvent.toJson());
            } else {
                // Case: New message on a chat room
                ChatRoomStream.Event event = (ChatRoomStream.Event) awaitResult;
                String json = event.toJson();
//                    Logger.debug("Sending event to " + user.username + ":" + connectionId + " - " + json);
                outbound.send(json);
            }
        }

        private static void processWebsocketClose(JsonUser user, String connectionId, HashMap<String, RoomConnection> roomConnections) {
            // Case: The socket has been closed
            Logger.info("Socket closed: " + user.username + ":" + connectionId);
            for (RoomConnection roomConnection : roomConnections.values()) {
                ChatRoom.userNotPresent(roomConnection.room.name, user.username, connectionId);
                // If this was the last connection that user had to the room then broadcast they've left
                roomConnection.chatRoomEventStream.leave(roomConnection.room, user, connectionId);
                if (!ChatRoom.isUserPresent(roomConnection.room.name, user.username)) {
                    Logger.debug("Last connection for " + user.username + " on channel " + roomConnection.room.name + " disconnected, broadcasting leave.");
                }
            }
            disconnect();
        }

        private static void processWebsocketFrame(JsonUser user, String connectionId, HashMap<String, RoomConnection> roomConnections, WebSocketFrame awaitResult) {
            WebSocketFrame frame = awaitResult;
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
                                ChatRoom.userPresent(connection.room.name, user.username, connectionId);
                            }
                            // todo also add to global users connected set for easier stats
                            //                        Logger.debug("Ping msg - skipping.");
                        } else if (message.toLowerCase().equals("##memberlist##")) {
                            Logger.debug("User " + user.username + " requested member list.");

                            ChatRoom room = roomConnection.room.loadModelFromDatabase();
                            outbound.send(new ChatRoomStream.MemberList(roomConnection.room,
                                    room.getAllUsersWithOnlineStatus()).toJson());
                        } else if (message.toLowerCase().equals("##markmessagesread##")) {
                            Logger.debug("User " + user.username + " marking messages read for " + roomName);

                            ChatRoom.markMessagesReadForUser(roomConnection.room.name, user.username);
                        } else if (ChatCommands.isCommand(message)) {
                            try {
                                ChatRoom room = roomConnection.room.loadModelFromDatabase();
                                ChatUser execUser = user.loadModelFromDatabase();
                                ChatUser systemUser = ChatUser.getSystemUser();
                                ChatCommands.execCommand(execUser, room, message, roomConnection.chatRoomEventStream, outbound, systemUser);
                            } catch (ChatCommands.NotEnoughPermissionsException e) {
                                sendLocalServerMessage(roomConnection, "You don't have permission to execute this command.");
                            } catch (ChatCommands.CommandNotRecognizedException e) {
                                sendLocalServerMessage(roomConnection, "Command not recognized.");
                            }
                        } else {
                            if (roomConnection.canPost) {
                                String uuid = Util.getUUID();
                                JsonMessage jsonMessage = JsonMessage.makePresavedMessage(uuid, user.username, roomConnection.room.name, message);
                                new SaveNewMessageJob(uuid, user.username, roomName, message).now();
                                roomConnection.chatRoomEventStream.say(jsonMessage,  roomConnection.room, user);
                                Stats.count(Stats.StatKey.MESSAGE, 1);
                            } else {
                                Logger.info("User " + user.username + " cannot post to " + roomName);
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
        }

        private static void sendLocalServerMessage(RoomConnection roomConnection, String message) {
            outbound.send(new ChatRoomStream.ServerMessage(roomConnection.room, JsonUser.fromUser(ChatUser.getSystemUser(), true), message).toJson());
        }

        private static void addConnection(ChatUser userModel, JsonUser user, String connectionId, HashMap<String, RoomConnection> roomConnections, ChatRoom room) {
            ChatRoomStream chatRoomStreamForRoom = ChatRoomStream.getEventStream(room.name);

            // Socket connected, join the chat room
            // If this is the first connection this user has to the room then broadcast
            boolean broadcastJoin = !room.isUserPresent(user.username);
            if (broadcastJoin) {
                Logger.debug("First connection for " + user.username + ", broadcasting join for connectionId " + connectionId);
            }
            room.userPresent(user.username, connectionId);
            boolean isModerator = room.isModerator(userModel);
            EventStream<ChatRoomStream.Event> eventStreamForThisUser = chatRoomStreamForRoom.join(room, userModel, connectionId, broadcastJoin);

            roomConnections.put(room.name, new RoomConnection(JsonChatRoom.from(room, room.getModeratorUsernames()),
                    chatRoomStreamForRoom, eventStreamForThisUser, isModerator, room.userCanPost(userModel)));
        }

    }

}

