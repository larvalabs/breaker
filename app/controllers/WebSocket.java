package controllers;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.larvalabs.redditchat.ChatCommands;
import com.larvalabs.redditchat.Constants;
import com.larvalabs.redditchat.dataobj.*;
import com.larvalabs.redditchat.realtime.ChatRoomStream;
import com.larvalabs.redditchat.util.RedisUtil;
import com.larvalabs.redditchat.util.Stats;
import com.larvalabs.redditchat.util.Util;
import jobs.SaveNewMessageJob;
import models.ChatRoom;
import models.ChatUser;
import models.ChatUserRoomJoin;
import play.Logger;
import play.Play;
import play.libs.F.Promise;
import play.mvc.Http;
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
        TreeMap<String, JsonUser> allUsers = fullState.users;
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
        String lastSeenTimesString = gson.toJson(fullState.lastSeenTimes);
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
        Http.Request request = Http.Request.current();
        if("true".equals(request.params.get("dev"))){
            environment = "dev";
        }

        roomName = roomName.toLowerCase();

        Logger.info("Websocket join time for " + user.getUsername() + ": " + (System.currentTimeMillis() - startTime));

        render("index.html", user, rooms, userString, roomName, environment, roomsString, usersString, membersString,
                roomMessagesString, messagesString, lastSeenTimesString);
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
        public ChatRoomStream.WeakReferenceEventStream<ChatRoomStream.Event> eventStream;
        public boolean isModerator;
        public boolean canPost;

        public RoomConnection(JsonChatRoom room, ChatRoomStream chatRoomEventStream, ChatRoomStream.WeakReferenceEventStream<ChatRoomStream.Event> eventStream, boolean isModerator, boolean canPost) {
            this.room = room;
            this.chatRoomEventStream = chatRoomEventStream;
            this.eventStream = eventStream;
            this.isModerator = isModerator;
            this.canPost = canPost;
        }
    }

    private static class RoomConnectionManager {
        HashMap<String, RoomConnection> roomConnections = new HashMap<java.lang.String, RoomConnection>();
        List<RoomConnection> roomConnectionList = new ArrayList<>();
        Promise[] roomConnectionsPromises;
        Http.Inbound inbound;

        public RoomConnectionManager(Http.Inbound inbound) {
            this.inbound = inbound;
        }

        private void addConnection(ChatUser userModel, JsonUser user, String connectionId, ChatRoom room) {
            ChatRoomStream chatRoomStreamForRoom = ChatRoomStream.getEventStream(room.name);

            // Socket connected, join the chat room
            // If this is the first connection this user has to the room then broadcast
            boolean broadcastJoin = !room.isUserPresent(user.username);
/*
            if (broadcastJoin) {
                Logger.debug("First connection for " + user.username + ", broadcasting join for connectionId " + connectionId);
            }
*/
            room.userPresent(user.username, connectionId);
            boolean isModerator = room.isModerator(userModel);
            ChatRoomStream.WeakReferenceEventStream<ChatRoomStream.Event> eventStreamForThisUser = chatRoomStreamForRoom.join(room, userModel, connectionId, broadcastJoin);

            RoomConnection roomConnection = new RoomConnection(JsonChatRoom.from(room, room.getModeratorUsernames()),
                    chatRoomStreamForRoom, eventStreamForThisUser, isModerator, room.userCanPost(userModel));
            roomConnections.put(room.name, roomConnection);
            roomConnectionList.add(roomConnection);
        }

        private void removeConnection(String roomName) {
            RoomConnection roomConnection = roomConnections.remove(roomName);
            roomConnectionList.remove(roomConnection);
            roomConnectionsPromises = null;
        }

        public RoomConnection getRoom(String name) {
            return roomConnections.get(name);
        }

        public Iterator<RoomConnection> iterateRoomConnections() {
            return roomConnectionList.iterator();
        }

        public RoomConnection get(int index) {
            return roomConnectionList.get(index);
        }

        public Promise[] getPromises() {
            if (roomConnectionsPromises == null) {
                roomConnectionsPromises = new Promise[roomConnections.size() + 1];
                roomConnectionsPromises[0] = inbound.nextEvent();
                for (int i = 1; i < roomConnectionsPromises.length; i++) {
                    RoomConnection roomConnection = roomConnectionList.get(i - 1);
                    roomConnectionsPromises[i] = roomConnection.eventStream.nextEvent();
                }
            }
            return roomConnectionsPromises;
        }

        public void redeemPromise(int index) {
            if (index == 0) {
                roomConnectionsPromises[0] = inbound.nextEvent();
            } else {
                roomConnectionsPromises[index] = roomConnectionList.get(index - 1).eventStream.nextEvent();
            }
        }

        public void disconnect(JsonUser user, String connectionId) {
            for (RoomConnection roomConnection : roomConnectionList) {
                roomConnection.chatRoomEventStream.removeStream(roomConnection.room, user, connectionId);
            }
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

            Logger.debug(user.getUsername() + " connecting to websocket and " + chatRoomJoins.size() + " rooms.");

            RoomConnectionManager roomConnectionManager = new RoomConnectionManager(inbound);
            ArrayList<JsonChatRoom> jsonChatRoomsList = new ArrayList<JsonChatRoom>();

            // This is a single element array because the current json user can get updated by events in an inner loop
            JsonUser[] jsonUser = new JsonUser[]{JsonUser.fromUser(user, true)};

            {
                int i = 0;
                for (ChatUserRoomJoin chatRoomJoin : chatRoomJoins) {
                    ChatRoom room = chatRoomJoin.getRoom();
                    if (Constants.CHATROOM_DEFAULT.equals(room.name) || room.isOpen()) {
                        jsonChatRoomsList.add(JsonChatRoom.from(room, room.getModeratorUsernames()));
                        roomConnectionManager.addConnection(user, jsonUser[0], connectionId, room);
                        i++;
                    }
                }
            }

            Stats.count(Stats.StatKey.WEBSOCKET_CONNECT, 1);

            // Loop while the socket is open
            while (inbound.isOpen()) {
                ChatRoomStream.WaitAnyPromise waitAnyPromise = ChatRoomStream.waitAnyWithResultInfo(roomConnectionManager.getPromises());
                Object awaitResult = await(waitAnyPromise);

                // Case: TextEvent received on the socket
                if (awaitResult instanceof WebSocketFrame) {
                    processWebsocketFrame(jsonUser[0], connectionId, roomConnectionManager, (WebSocketFrame) awaitResult);
                } else if (awaitResult instanceof WebSocketClose) {
                    processWebsocketClose(jsonUser[0], connectionId, roomConnectionManager);
                } else if (awaitResult instanceof ChatRoomStream.Event) {
                    processEvent(jsonUser, roomConnectionManager, (ChatRoomStream.Event) awaitResult, connectionId);
                }

                int indexRedeemed = waitAnyPromise.getIndexRedeemed();
                roomConnectionManager.redeemPromise(indexRedeemed);
            }

            // Just to be sure, in case we didn't get a proper disconnect
            roomConnectionManager.disconnect(jsonUser[0], connectionId);
            RedisUtil.userNotPresentGlobal(jsonUser[0].username, connectionId);
        }

        private static void processEvent(JsonUser[] userArray, RoomConnectionManager roomConnectionManager, ChatRoomStream.Event awaitResult, String connectionId) {
            JsonUser thisConnectionUser = userArray[0];

            // Bail out if this is directed a specific user, don't process
            if (awaitResult.toUsername != null && !awaitResult.toUsername.equals(thisConnectionUser.username)) {
                return;
            }

            if (awaitResult instanceof ChatRoomStream.ServerCommand) {
                // Case: A command affecting users
                ChatRoomStream.ServerCommand commandEvent = (ChatRoomStream.ServerCommand) awaitResult;
                if (commandEvent.command.username != null && commandEvent.command.username.equals(thisConnectionUser.username)) {
                    Logger.info("Received " + commandEvent.command.type + " for this user.");
                    if (commandEvent.command.type.shouldCloseClientSocket()) {
                        Logger.info(thisConnectionUser.username + " has been disconnected from " + commandEvent.room.name);

                        RoomConnection roomConnection = roomConnectionManager.getRoom(commandEvent.room.name);
                        ChatRoom roomModel = roomConnection.room.loadModelFromDatabase();
                        ChatUser userModel = thisConnectionUser.loadModelFromDatabase();

                        userModel.leaveChatRoom(roomModel);
                        roomConnectionManager.removeConnection(commandEvent.room.name);

                        sendLocalServerMessage(roomConnection, thisConnectionUser.username, commandEvent.command.username + " was kicked.");
                        disconnect();
                    }
                }
            } else if (awaitResult instanceof ChatRoomStream.UpdateUserEvent) {
                ChatRoomStream.UpdateUserEvent updateEvent = (ChatRoomStream.UpdateUserEvent) awaitResult;
                if (updateEvent.user.equals(thisConnectionUser)) {
                    Logger.info("Updated local user object from event: " + updateEvent.user.username);
                    userArray[0] = updateEvent.user;
                }
                outbound.send(updateEvent.toJson());
            } else if (awaitResult instanceof ChatRoomStream.UpdateRoomEvent) {
                ChatRoomStream.UpdateRoomEvent updateEvent = (ChatRoomStream.UpdateRoomEvent) awaitResult;
                if (roomConnectionManager.roomConnections.containsKey(updateEvent.room.name)) {
                    Logger.info("Updated local room object from event: " + updateEvent.room.name);
                    roomConnectionManager.getRoom(updateEvent.room.name).room = updateEvent.room;
                }
                outbound.send(updateEvent.toJson());
            } else if (awaitResult instanceof ChatRoomStream.UpdateMessageEvent) {
                ChatRoomStream.UpdateMessageEvent updateEvent = (ChatRoomStream.UpdateMessageEvent) awaitResult;
                if (roomConnectionManager.roomConnections.containsKey(updateEvent.room.name)) {
                    Logger.info("Updated local room object from event: " + updateEvent.room.name);
                    roomConnectionManager.getRoom(updateEvent.room.name).room = updateEvent.room;
                }
                outbound.send(updateEvent.toJson());
            } else if (awaitResult instanceof ChatRoomStream.Leave) {
                ChatRoomStream.Leave event = (ChatRoomStream.Leave) awaitResult;
                outbound.send(awaitResult.toJson());
                if (event.user.username.equalsIgnoreCase(thisConnectionUser.username)) {
                    RoomConnection roomConnection = roomConnectionManager.getRoom(event.room.name);
                    roomConnection.chatRoomEventStream.removeStream(event.room, event.user, connectionId);
                }
            } else if (awaitResult instanceof ChatRoomStream.RoomLeave) {
                ChatRoomStream.RoomLeave event = (ChatRoomStream.RoomLeave) awaitResult;
                if (event.user.username.equalsIgnoreCase(thisConnectionUser.username)) {
                    RoomConnection roomConnection = roomConnectionManager.getRoom(event.room.name);
                    roomConnection.chatRoomEventStream.removeStream(event.room, event.user, connectionId);
                    outbound.send(awaitResult.toJson());
                } else {
                    outbound.send(new ChatRoomStream.UserLeave(event.room, event.user).toJson());
                }
            } else {
                // Case: New message on a chat room
                ChatRoomStream.Event event = (ChatRoomStream.Event) awaitResult;
                String json = event.toJson();
//                    Logger.debug("Sending event to " + user.username + ":" + connectionId + " - " + json);
                outbound.send(json);
            }
        }

        private static void processWebsocketClose(JsonUser user, String connectionId, RoomConnectionManager roomConnectionManager) {
            // Case: The socket has been closed
            Logger.info("Socket closed: " + user.username + ":" + connectionId);
            for (RoomConnection roomConnection : roomConnectionManager.roomConnectionList) {
                RedisUtil.userNotPresent(roomConnection.room.name, user.username, connectionId);
                user.online = false;
                roomConnection.chatRoomEventStream.removeStream(roomConnection.room, user, connectionId);
                if (!RedisUtil.isUserPresent(roomConnection.room.name, user.username)) {
//                    Logger.debug("Last connection for " + user.username + " on channel " + roomConnection.room.name + " disconnected, broadcasting leave.");
                    // If this was the last connection that user had to the room then broadcast they've left
                    roomConnection.chatRoomEventStream.leave(roomConnection.room, user);
                }
            }
            disconnect();
        }

        private static void processWebsocketFrame(JsonUser user, String connectionId, RoomConnectionManager roomConnectionManager, WebSocketFrame awaitResult) {
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
                    RoomConnection roomConnection = roomConnectionManager.getRoom(roomName);
                    if (roomConnection != null) {
                        if (message.toLowerCase().equals("##ping##")) {
                            for (RoomConnection connection : roomConnectionManager.roomConnectionList) {
                                RedisUtil.userPresent(connection.room.name, user.username, connectionId);
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

                            long newLastReadTime = ChatRoom.markMessagesReadForUser(roomConnection.room.name, user.username);
                            roomConnection.chatRoomEventStream.publishEvent(new ChatRoomStream.MarkedRead(roomConnection.room, user.username, newLastReadTime));
                        } else if (ChatCommands.isCommand(message)) {
                            try {
                                ChatRoom room = roomConnection.room.loadModelFromDatabase();
                                ChatUser execUser = user.loadModelFromDatabase();
                                ChatUser systemUser = ChatUser.getSystemUser();
                                ChatCommands.execCommand(execUser, room, message, roomConnection.chatRoomEventStream, outbound, systemUser);
                            } catch (ChatCommands.NotEnoughPermissionsException e) {
                                sendLocalServerMessage(roomConnection, user.username, "You don't have permission to execute this command.");
                            } catch (ChatCommands.CommandNotRecognizedException e) {
                                sendLocalServerMessage(roomConnection, user.username, "Command not recognized.");
                            }
                        } else {
                            if (roomConnection.canPost) {
                                String uuid = Util.getUUID();
                                JsonMessage jsonMessage = JsonMessage.makePresavedMessage(uuid, user.username, roomConnection.room.name, message);
                                // Disabled for now until can figure out double sending problem
//                                outbound.send(new ChatRoomStream.Message(jsonMessage, roomConnection.room, user).toJson());
                                new SaveNewMessageJob(uuid, user.username, roomName, message, jsonMessage.createDate).now();
                                roomConnection.chatRoomEventStream.say(jsonMessage, roomConnection.room, user);
                                Stats.count(Stats.StatKey.MESSAGE, 1);
                            } else {
                                Logger.info("User " + user.username + " cannot post to " + roomName);
                                // Direct message to user who tried to send this
                                sendLocalServerMessage(roomConnection, user.username, "You cannot post to this room.");
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

        private static void sendLocalServerMessage(RoomConnection roomConnection, String toUsername, String message) {
            outbound.send(new ChatRoomStream.ServerMessage(roomConnection.room, toUsername, JsonUser.fromUser(ChatUser.getSystemUser(), true), message).toJson());
        }
    }

}

