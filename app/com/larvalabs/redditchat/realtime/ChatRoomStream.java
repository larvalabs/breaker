package com.larvalabs.redditchat.realtime;

import java.util.*;

import com.google.gson.Gson;
import com.larvalabs.redditchat.dataobj.JsonMessage;
import com.larvalabs.redditchat.dataobj.JsonUser;
import models.ChatRoom;
import play.Logger;
import play.libs.F.*;

public class ChatRoomStream {

    private String name;

    public ChatRoomStream(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    // ~~~~~~~~~ Let's chat!
    
    final ArchivedEventStream<ChatRoomStream.Event> chatEvents = new ArchivedEventStream<ChatRoomStream.Event>(100);
    
    /**
     * For WebSocket, when a user join the room we return a continuous event stream
     * of ChatEvent
     */
    public EventStream<ChatRoomStream.Event> join(ChatRoom room, JsonUser user, boolean broadcastJoin) {
        if (broadcastJoin) {
            chatEvents.publish(new Join(user));
        }
        return chatEvents.eventStream();
    }

    public void sendMemberList(ChatRoom room) {
        String[] usernames = room.getUsernamesPresent().toArray(new String[]{});
        Logger.info("Sending member list of length " + usernames.length);
        chatEvents.publish(new MemberList(usernames));
    }
    
    /**
     * A user leave the room
     */
    public void leave(JsonUser user) {
        chatEvents.publish(new Leave(user));
    }
    
    /**
     * A user say something on the room
     */
    public void say(JsonMessage message) {
        // todo maybe move this empty check elsewhere?
        if(message.message == null || message.message.trim().equals("")) {
            return;
        }
        chatEvents.publish(new Message(message));
    }
    
    /**
     * For long polling, as we are sometimes disconnected, we need to pass 
     * the last event seen id, to be sure to not miss any message
     */
    public Promise<List<IndexedEvent<ChatRoomStream.Event>>> nextMessages(long lastReceived) {
        return chatEvents.nextEvents(lastReceived);
    }
    
    /**
     * For active refresh, we need to retrieve the whole message archive at
     * each refresh
     */
    public List<ChatRoomStream.Event> archive() {
        return chatEvents.archive();
    }
    
    // ~~~~~~~~~ Chat room events

    public static abstract class Event {
        
        final public String type;
        final public Long timestamp;
        
        public Event(String type) {
            this.type = type;
            this.timestamp = System.currentTimeMillis();
        }

        public String toJson() {
            String json = new Gson().toJson(this);
            return json;
        }
    }

    public static class MemberList extends Event {

        final public String[] usernames;

        public MemberList(String[] usernames) {
            super("memberlist");
            this.usernames = usernames;
        }
    }
    
    public static class Join extends Event {
        
        final public JsonUser user;
        
        public Join(JsonUser user) {
            super("join");
            this.user = user;
        }
        
    }
    
    public static class Leave extends Event {
        
        final public JsonUser user;
        
        public Leave(JsonUser user) {
            super("leave");
            this.user = user;
        }
        
    }
    
    public static class Message extends Event {

        final public JsonUser user;         // Just for convenience on the front end
        final public JsonMessage message;

        public Message(JsonMessage message) {
            super("message");
            this.message = message;
            this.user = this.message.user;
        }
        
    }
    
    // ~~~~~~~~~ Chat room factory

    private static HashMap<String, ChatRoomStream> rooms = new HashMap<String, ChatRoomStream>();

    public static ChatRoomStream get(String name) {
        ChatRoomStream chatRoomStream = rooms.get(name);
        if (chatRoomStream == null) {
            chatRoomStream = new ChatRoomStream(name);
            rooms.put(name, chatRoomStream);
        }
        return chatRoomStream;
    }
    
}

