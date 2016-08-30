package com.larvalabs.redditchat.dataobj;

import com.larvalabs.redditchat.Constants;
import com.larvalabs.redditchat.realtime.ChatRoomStream;
import com.larvalabs.redditchat.util.Stats;
import models.ChatRoom;
import models.Message;
import org.apache.commons.collections4.map.LRUMap;
import play.Logger;

import java.util.*;

/**
 * Created by matt on 4/26/16.
 */
public class BreakerCache {

    public static final boolean CACHE_ENABLED = true;
    public static Random random = new Random();

    private static HashMap<String, TreeSet<JsonMessage>> messageCache = new HashMap<String, TreeSet<JsonMessage>>();
    private static LRUMap<String, JsonUser> userCache = new LRUMap<>(1000);
    private static LRUMap<String, JsonChatRoom> roomCache = new LRUMap<>(1000);

    private static final String KEY_MESSAGES = "messages-";
    private static final String KEY_USER = "user-";

    private static String getMessagesKey(String roomName) {
        return KEY_MESSAGES + roomName;
    }

/*
    public static void clearMessagesCache(String roomName) {
        synchronized (messageCache) {
            messageCache.remove(getMessagesKey(roomName));
        }
        Logger.info("Clear message cache for " + roomName);
    }
*/

    public static void addMessageToCache(Message message) {
        JsonMessage jsonMsg = JsonMessage.from(message, message.getUser().getUsername(), message.getRoom().getName());
        addToCacheInternal(jsonMsg);
    }

    /**
     * Preferred over the Message alternative because it saves a conversion to JsonMessage.
     * @param message
     */
    public static void addMessageToCache(JsonMessage message) {
        addToCacheInternal(message);
    }

    private static void addToCacheInternal(JsonMessage jsonMsg) {
        synchronized (messageCache) {
            TreeSet<JsonMessage> jsonMessages = messageCache.get(getMessagesKey(jsonMsg.roomName));
            if (jsonMessages == null) {
                jsonMessages = makeNewMessageSet();
                messageCache.put(getMessagesKey(jsonMsg.roomName), jsonMessages);
            }
            jsonMessages.remove(jsonMsg);
            jsonMessages.add(jsonMsg);
            if (jsonMessages.size() > Constants.DEFAULT_MESSAGE_LIMIT) {
                jsonMessages.remove(jsonMessages.first());
            }
        }
    }

    private static void addAllToCacheInternal(String roomName, Collection<JsonMessage> messages) {
        synchronized (messageCache) {
            TreeSet<JsonMessage> jsonMessages = messageCache.get(getMessagesKey(roomName));
            if (jsonMessages == null) {
                jsonMessages = makeNewMessageSet();
                jsonMessages.addAll(messages);
                messageCache.put(getMessagesKey(roomName), jsonMessages);
            } else {
                jsonMessages.removeAll(messages);
                jsonMessages.addAll(messages);
            }
            while (jsonMessages.size() > Constants.DEFAULT_MESSAGE_LIMIT) {
                jsonMessages.remove(jsonMessages.first());
            }
        }
    }

    public static TreeSet<JsonMessage> getLastMessages(ChatRoom room) {
        TreeSet<JsonMessage> roomMessages = messageCache.get(getMessagesKey(room.getName()));
        if (roomMessages == null || !CACHE_ENABLED) {
            Logger.info("Cache miss room messages for " + room.getName());
            return loadLastJsonMessages(room);
        } else {
//                Logger.info("Cache hit room messages for " + room.getName());
        }
        return new TreeSet<>(roomMessages);
    }

    public static void preloadCacheForRoom(ChatRoom room) {
        loadLastJsonMessages(room);
    }

    /**
     * Load latest messages and cache as messages are loaded.
     * @param room
     * @return
     */
    private static TreeSet<JsonMessage> loadLastJsonMessages(ChatRoom room) {
        TreeSet<JsonMessage> roomMessages = makeNewMessageSet();
        List<Message> messageList = room.getMessages(Constants.DEFAULT_MESSAGE_LIMIT);
        for (Message message : messageList) {
            JsonMessage jsonMessage = JsonMessage.from(message, message.getUser().getUsername(), room.getName());
            if (message.isHasLinks()) {
                jsonMessage.setLinkInfo(message.getLinks());
            }
            roomMessages.add(jsonMessage);
        }
        addAllToCacheInternal(room.getName(), roomMessages);
//        Collections.reverse(roomMessages);
        return roomMessages;
    }

    private static TreeSet<JsonMessage> makeNewMessageSet() {
        return new TreeSet<>(new Comparator<JsonMessage>() {
            @Override
            public int compare(JsonMessage o1, JsonMessage o2) {
                return Long.compare(o1.createDateLongUTC, o2.createDateLongUTC);
            }
        });
    }

    public static void clearUsersCacheAll() {
        Logger.info("Clearing all user cache values.");
        synchronized (userCache) {
            userCache.clear();
        }
    }

    public static void clearUsersCache(String roomName) {
        userCache.remove(roomName);
    }

    public static JsonUser getJsonUser(String username) {
        synchronized (userCache) {
            return userCache.get(username);
        }
    }

    public static void putJsonUser(JsonUser user) {
        synchronized (userCache) {
            if (random.nextFloat() < 0.1f) {
                Stats.sample(Stats.StatKey.USER_CACHE_SIZE, userCache.size());
            }
            userCache.put(user.username, user);
        }
    }

    public static void removeUser(String username) {
        synchronized (userCache) {
            userCache.remove(username);
        }
    }

    public static void clearRoomCacheAll() {
        Logger.info("Clearing all room cache values.");
        synchronized (roomCache) {
            roomCache.clear();
        }
    }

    public static void clearRoomCache(String roomName) {
        synchronized (roomCache) {
            roomCache.remove(roomName);
        }
    }

    public static JsonChatRoom getJsonChatRoom(String name) {
        synchronized (roomCache) {
            return roomCache.get(name);
        }
    }

    public static void putJsonChatRoom(JsonChatRoom room) {
        synchronized (roomCache) {
            if (random.nextFloat() < 0.1f) {
                Stats.sample(Stats.StatKey.ROOM_CACHE_SIZE, roomCache.size());
            }
            roomCache.put(room.name, room);
        }
    }

    public static void removeChatRoom(String name) {
        synchronized (roomCache) {
            roomCache.remove(name);
        }
    }

    public static void handleEvent(ChatRoomStream.Event serverEvent) {
        if (serverEvent instanceof ChatRoomStream.Message && !(serverEvent instanceof ChatRoomStream.ServerMessage)) {
            ChatRoomStream.Message event = (ChatRoomStream.Message) serverEvent;
            addMessageToCache(event.message);
            Logger.info("Added message id " + event.message.uuid + " to cache.");
//            clearMessagesCache(event.room.name);
            putJsonUser(event.user);
        } else if (serverEvent instanceof ChatRoomStream.UpdateUserEvent) {
            ChatRoomStream.UpdateUserEvent event = (ChatRoomStream.UpdateUserEvent) serverEvent;
            putJsonUser(event.user);
        } else if (serverEvent instanceof ChatRoomStream.Join) {
            ChatRoomStream.Join event = (ChatRoomStream.Join) serverEvent;
            putJsonUser(event.user);
        } else if (serverEvent instanceof ChatRoomStream.Leave) {
            ChatRoomStream.Leave event = (ChatRoomStream.Leave) serverEvent;
            putJsonUser(event.user);
        } else if (serverEvent instanceof ChatRoomStream.RoomLeave) {
            ChatRoomStream.RoomLeave event = (ChatRoomStream.RoomLeave) serverEvent;
            putJsonUser(event.user);
        } else if (serverEvent instanceof ChatRoomStream.UpdateRoomEvent) {
            ChatRoomStream.UpdateRoomEvent event = (ChatRoomStream.UpdateRoomEvent) serverEvent;
            putJsonChatRoom(event.room);
        }
    }

}
