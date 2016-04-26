package com.larvalabs.redditchat.dataobj;

import com.larvalabs.redditchat.Constants;
import models.ChatRoom;
import models.Message;
import play.cache.Cache;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by matt on 4/26/16.
 */
public class BreakerCache {

    private static final String KEY_MESSAGES = "messages-";

    private static String getMessagesKey(String roomName) {
        return KEY_MESSAGES + roomName;
    }

    public static void clearMessagesCache(String roomName) {
        Cache.delete(getMessagesKey(roomName));
    }

    public static ArrayList<JsonMessage> getLastMessages(ChatRoom room) {
        ArrayList<JsonMessage> roomMessages = null;
        try {
            roomMessages = Cache.get(getMessagesKey(room.getName()), ArrayList.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (roomMessages == null) {
            roomMessages = new ArrayList<JsonMessage>();
            List<Message> messageList = room.getMessages(Constants.DEFAULT_MESSAGE_LIMIT);
            for (Message message : messageList) {
                roomMessages.add(JsonMessage.from(message));
            }
            Collections.reverse(roomMessages);
            Cache.set(getMessagesKey(room.getName()), roomMessages, "1h");
        }
        return roomMessages;
    }
}
