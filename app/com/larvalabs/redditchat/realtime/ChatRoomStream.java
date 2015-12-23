package com.larvalabs.redditchat.realtime;

import java.util.*;

import com.larvalabs.redditchat.dataobj.JsonMessage;
import com.larvalabs.redditchat.dataobj.JsonUser;
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
    public EventStream<ChatRoomStream.Event> join(JsonUser user) {
        chatEvents.publish(new Join(user));
        return chatEvents.eventStream();
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

        final public JsonMessage message;

        public Message(JsonMessage message) {
            super("message");
            this.message = message;
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

