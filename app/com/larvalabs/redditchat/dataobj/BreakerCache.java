package com.larvalabs.redditchat.dataobj;

import com.larvalabs.redditchat.Constants;
import com.larvalabs.redditchat.realtime.ChatRoomStream;
import models.ChatRoom;
import models.Message;
import play.Logger;
import play.cache.Cache;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * Created by matt on 4/26/16.
 */
public class BreakerCache {

    private static HashMap<String, ArrayList<JsonMessage>> messageCache = new HashMap<String, ArrayList<JsonMessage>>();

    private static final String KEY_MESSAGES = "messages-";

    private static String getMessagesKey(String roomName) {
        return KEY_MESSAGES + roomName;
    }

    public static void clearMessagesCache(String roomName) {
        messageCache.remove(getMessagesKey(roomName));
        Logger.info("Clear message cache for " + roomName);
    }

    public static ArrayList<JsonMessage> getLastMessages(ChatRoom room) {
        ArrayList<JsonMessage> roomMessages = messageCache.get(getMessagesKey(room.getName()));
        if (roomMessages == null) {
            Logger.info("Cache miss room messages for " + room.getName());
            roomMessages = new ArrayList<JsonMessage>();
            List<Message> messageList = room.getMessages(Constants.DEFAULT_MESSAGE_LIMIT);
            for (Message message : messageList) {
                roomMessages.add(JsonMessage.from(message));
            }
            Collections.reverse(roomMessages);
            messageCache.put(getMessagesKey(room.getName()), roomMessages);
        } else {
            Logger.info("Cache hit room messages for " + room.getName());
        }
        return roomMessages;
    }

    public static void handleEvent(ChatRoomStream.Event event) {
        if (event instanceof ChatRoomStream.Message) {
            ChatRoomStream.Message messageEvent = (ChatRoomStream.Message) event;
            clearMessagesCache(messageEvent.room.name);
        }
    }
}
