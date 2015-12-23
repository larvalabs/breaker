package controllers;

import com.larvalabs.redditchat.realtime.ChatRoomStream;
import jobs.SaveNewMessageJob;
import play.*;
import play.mvc.*;
import play.libs.F.*;
import play.mvc.Http.*;

import static play.libs.F.Matcher.*;
import static play.mvc.Http.WebSocketEvent.*;

import models.*;

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
        render(user, roomName);
    }

    public static class ChatRoomSocket extends WebSocketController {

        public static void join(String roomName) {

            ChatUser user = getUser();
            if (user == null) {
                disconnect();
                return;
            }

            ChatRoomStream room = ChatRoomStream.get(roomName);

            // Socket connected, join the chat room
            EventStream<ChatRoomStream.Event> roomMessagesStream = room.join(user.username);

            // Loop while the socket is open
            while (inbound.isOpen()) {

                // Wait for an event (either something coming on the inbound socket channel, or ChatRoom messages)
                Either<WebSocketEvent, ChatRoomStream.Event> e = await(Promise.waitEither(
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
                    if (userMessage != null && userMessage.toLowerCase().equals("##ping##")) {
//                        Logger.debug("Ping msg - skipping.");
                    } else {
                        new SaveNewMessageJob(user, roomName, userMessage).now();
                        room.say(user.username, userMessage);
                    }
                }

                // Case: New message on the chat room
                if (e._2.isDefined()) {
                    ChatRoomStream.Event event = e._2.get();
                    if (event instanceof ChatRoomStream.Join) {
                        ChatRoomStream.Join joined = (ChatRoomStream.Join) event;
                        outbound.send("join:%s", joined.user);
                    } else if (event instanceof ChatRoomStream.Message) {
                        ChatRoomStream.Message message = (ChatRoomStream.Message) event;
                        outbound.send("message:%s:%s", message.user, message.text);
                    } else if (event instanceof  ChatRoomStream.Leave) {
                        ChatRoomStream.Leave left = (ChatRoomStream.Leave) event;
                        outbound.send("leave:%s", left.user);
                    }
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
                    room.leave(user.username);
                    disconnect();
                }

            }

        }

    }

}

