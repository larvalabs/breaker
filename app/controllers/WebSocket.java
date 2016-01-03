package controllers;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.larvalabs.redditchat.Constants;
import com.larvalabs.redditchat.dataobj.JsonChatRoom;
import com.larvalabs.redditchat.dataobj.JsonMessage;
import com.larvalabs.redditchat.dataobj.JsonUser;
import com.larvalabs.redditchat.realtime.ChatRoomStream;
import com.larvalabs.redditchat.util.Util;
import jobs.SaveNewMessageJob;
import play.*;
import play.libs.F;
import play.libs.F.*;
import play.mvc.Controller;
import play.mvc.Http.*;

import static play.libs.F.Matcher.*;
import static play.mvc.Http.WebSocketEvent.*;

import models.*;
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
        if (user == null) {
            Application.preAuthForRoomJoin(roomName);
            return;
        }
        if (roomName == null) {
            if (user.getChatRoomJoins().size() == 0) {
                user.joinChatRoom(ChatRoom.findByName(Constants.CHATROOM_DEFAULT));
            }
        } else {
            user.joinChatRoom(ChatRoom.findByName(roomName));
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

        public RoomConnection(ChatRoom room, ChatRoomStream roomStream, EventStream<ChatRoomStream.Event> eventStream) {
            this.room = room;
            this.roomStream = roomStream;
            this.eventStream = eventStream;
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

            {
                JsonChatRoom[] jsonChatRooms = new JsonChatRoom[chatRoomJoins.size()];
                int i = 0;
                for (ChatUserRoomJoin chatRoomJoin : chatRoomJoins) {
                    ChatRoom room = chatRoomJoin.getRoom();
                    jsonChatRooms[i] = JsonChatRoom.from(room);
                    Logger.debug("Connecting to chat room stream for " + room.getName());
                    addConnection(user, connectionId, roomConnections, room);
                    i++;
                }
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

                Promise<ChatRoomStream.Event>[] roomEventPromises = new Promise[roomConnections.size()];
                int i = 0;
                for (RoomConnection roomConnection : roomConnections.values()) {
                    roomEventPromises[i] = roomConnection.eventStream.nextEvent();
                    i++;
                }

                // Wait for an event (either something coming on the inbound socket channel, or ChatRoom messages)
                Either<WebSocketEvent, ChatRoomStream.Event> e = await(Promise.waitEither(
                        inbound.nextEvent(),
                        Promise.waitAny(roomEventPromises)
                ));

                // Case: TextEvent received on the socket
                for (String userMessageJson : TextFrame.match(e._1)) {
                    try {
                        Logger.debug("Message received from socket (" + user.username + "): " + userMessageJson);
                        JsonObject msgObj = new JsonParser().parse(userMessageJson).getAsJsonObject();
                        String roomName = msgObj.get("roomName").getAsString();
                        String message = msgObj.get("message").getAsString();
                        RoomConnection roomConnection = roomConnections.get(roomName);
                        if (roomConnection != null) {
                            if (message.toLowerCase().equals("##ping##")) {
                                roomConnection.room.userPresent(user, connectionId);
    //                        Logger.debug("Ping msg - skipping.");
                            } else if (message.toLowerCase().equals("##memberlist##")) {
                                Logger.debug("User " + user.username + " requested member list.");
                                outbound.send(new ChatRoomStream.MemberList(JsonChatRoom.from(roomConnection.room, user),
                                        roomConnection.room.getUsernamesPresent().toArray(new String[]{})).toJson());
                            } else {
                                String uuid = Util.getUUID();
                                JsonMessage jsonMessage = JsonMessage.makePresavedMessage(uuid, user, roomConnection.room, message);
                                new SaveNewMessageJob(uuid, user, roomName, message).now();
                                roomConnection.roomStream.say(jsonMessage);
                            }
                        } else {
                            Logger.error("Could not find room connection.");
                        }
                    } catch (Exception e1) {
                        Logger.error(e1, "Error handling user message, discarding.");
                    }
                }

                // Case: New message on a chat room
                if (e._2.isDefined()) {
                    ChatRoomStream.Event event = e._2.get();
                    String json = event.toJson();
                    Logger.debug("Sending event to " + user.username + ":"+connectionId +" - " +json);
                    outbound.send(json);

                }

                // Note: This for loop stuff is the way that the play guys try to avoid
                // casting and things like that. I dunno. I just replaced it with some
                // ifs because I found it hard to read.
/*
                // Case: Someone joined the room
                for (ChatRoomStream.Join joined : ClassOf(ChatRoomStream.Join.class).match(e._2)) {
                    outbound.send("join:%s", joined.user);
                }

                for (ChatRoomStream.Message message : ClassOf(ChatRoomStream.Message.class).match(e._2)) {
                    new SaveNewMessageJob(user, roomName, message.text);
                    outbound.send("message:%s:%s", message.user, message.text);
                }

                // Case: Someone left the room
                for (ChatRoomStream.Leave left : ClassOf(ChatRoomStream.Leave.class).match(e._2)) {
                    outbound.send("leave:%s", left.user);
                }
*/

                // Case: The socket has been closed
                for (WebSocketClose closed : SocketClosed.match(e._1)) {
                    for (RoomConnection roomConnection : roomConnections.values()) {
                        roomConnection.room.userNotPresent(user, connectionId);
                        // If this was the last connection that user had to the room then broadcast they've left
                        if (!roomConnection.room.isUserPresent(user)) {
                            Logger.debug("Last connection for " + user.username + " on channel " + roomConnection.room.getName() + " disconnected, broadcasting leave.");
                            roomConnection.roomStream.leave(roomConnection.room, user);
                        }
                    }
                    disconnect();
                }

            }

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

            roomConnections.put(room.name, new RoomConnection(room, roomStream, roomMessagesStream));
        }

    }

}

