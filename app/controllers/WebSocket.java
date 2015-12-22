package controllers;

import play.*;
import play.mvc.*;
import play.libs.*;
import play.libs.F.*;
import play.mvc.Http.*;

import static play.libs.F.*;
import static play.libs.F.Matcher.*;
import static play.mvc.Http.WebSocketEvent.*;

import java.util.*;

import models.*;

public class WebSocket extends Controller {

    public static void room(String user) {
        render(user);
    }

    public static class ChatRoomSocket extends WebSocketController {

        public static void join() {

            ChatUser user;
            if (session.contains("uid")) {
                String uid = session.get(Application.SESSION_UID);
                Logger.info("websocket found existing user: " + uid);
                user = ChatUser.get(uid);
            } else {
                Logger.info("No user id in session.");
                disconnect();
                return;
            }

            ChatRoom room = ChatRoom.get();

            // Socket connected, join the chat room
            EventStream<ChatRoom.Event> roomMessagesStream = room.join(user.username);

            // Loop while the socket is open
            while (inbound.isOpen()) {

                // Wait for an event (either something coming on the inbound socket channel, or ChatRoom messages)
                Either<WebSocketEvent, ChatRoom.Event> e = await(Promise.waitEither(
                        inbound.nextEvent(),
                        roomMessagesStream.nextEvent()
                ));

                // Case: User typed 'quit'
                for (String userMessage : TextFrame.and(Equals("quit")).match(e._1)) {
                    room.leave(user.username);
                    outbound.send("quit:ok");
                    disconnect();
                }

                // Case: TextEvent received on the socket
                for (String userMessage : TextFrame.match(e._1)) {
                    room.say(user.username, userMessage);
                }

                // Case: Someone joined the room
                for (ChatRoom.Join joined : ClassOf(ChatRoom.Join.class).match(e._2)) {
                    outbound.send("join:%s", joined.user);
                }

                // Case: New message on the chat room
                for (ChatRoom.Message message : ClassOf(ChatRoom.Message.class).match(e._2)) {
                    outbound.send("message:%s:%s", message.user, message.text);
                }

                // Case: Someone left the room
                for (ChatRoom.Leave left : ClassOf(ChatRoom.Leave.class).match(e._2)) {
                    outbound.send("leave:%s", left.user);
                }

                // Case: The socket has been closed
                for (WebSocketClose closed : SocketClosed.match(e._1)) {
                    room.leave(user.username);
                    disconnect();
                }

            }

        }

    }

}

