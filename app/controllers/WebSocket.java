package controllers;

import com.larvalabs.redditchat.Constants;
import com.larvalabs.redditchat.dataobj.JsonChatRoom;
import com.larvalabs.redditchat.dataobj.JsonMessage;
import com.larvalabs.redditchat.dataobj.JsonUser;
import com.larvalabs.redditchat.realtime.ChatRoomStream;
import com.larvalabs.redditchat.util.Util;
import jobs.SaveNewMessageJob;
import play.*;
import play.libs.F.*;
import play.mvc.Controller;
import play.mvc.Http.*;

import static play.libs.F.Matcher.*;
import static play.mvc.Http.WebSocketEvent.*;

import models.*;
import play.mvc.WebSocketController;

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
        render("WebSocket/room3.html", user, roomName);
    }

    /**
     * Just used to wake up the server after restart since the websocket controller
     * doesn't seem to count
     */
    public static void wakeup() {
        renderText("ok");
    }

    public static class ChatRoomSocket extends WebSocketController {

        public static void join(String roomName) {

            ChatUser user = getUser();
            if (user == null) {
                disconnect();
                return;
            }

            String connectionId = Util.getUUID();
            ChatRoom room = ChatRoom.findByName(roomName);
            ChatRoomStream roomStream = ChatRoomStream.get(roomName);

            // Socket connected, join the chat room
            // If this is the first connection this user has to the room then broadcast
            boolean broadcastJoin = !room.isUserPresent(user);
            if (broadcastJoin) {
                Logger.debug("First connection for " + user.username + ", broadcasting join for connectionId " + connectionId);
            }
            room.userPresent(user, connectionId);
            EventStream<ChatRoomStream.Event> roomMessagesStream = roomStream.join(room, user, broadcastJoin);

            // Loop while the socket is open
            while (inbound.isOpen()) {

                // Wait for an event (either something coming on the inbound socket channel, or ChatRoom messages)
                Either<WebSocketEvent, ChatRoomStream.Event> e = await(Promise.waitEither(
                        inbound.nextEvent(),
                        roomMessagesStream.nextEvent()
                ));

                // Case: User typed 'quit'
                for (String userMessage : TextFrame.and(Equals("quit")).match(e._1)) {
                    roomStream.leave(room, user);
                    outbound.send("quit:ok");
                    disconnect();
                }

                // Case: TextEvent received on the socket
                for (String userMessage : TextFrame.match(e._1)) {
                    if (userMessage.toLowerCase().equals("##ping##")) {
                        room.userPresent(user, connectionId);
//                        Logger.debug("Ping msg - skipping.");
                    } else if (userMessage.toLowerCase().equals("##memberlist##")) {
                        Logger.debug("User " + user.username + " requested member list.");
                        outbound.send(new ChatRoomStream.MemberList(JsonChatRoom.from(room, user), room.getUsernamesPresent().toArray(new String[]{})).toJson());
                    } else {
                        String uuid = com.larvalabs.redditchat.util.Util.getUUID();
                        JsonMessage jsonMessage = JsonMessage.makePresavedMessage(uuid, user, room, userMessage);
                        new SaveNewMessageJob(uuid, user, roomName, userMessage).now();
                        roomStream.say(jsonMessage);
                    }
                }

                // Case: New message on the chat room
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
                    room.userNotPresent(user, connectionId);
                    // If this was the last connection that user had to the room then broadcast they've left
                    if (!room.isUserPresent(user)) {
                        Logger.debug("Last connection for " + user.username + " disconnected, broadcasting leave.");
                        roomStream.leave(room, user);
                    }
                    disconnect();
                }

            }

        }

    }

}

