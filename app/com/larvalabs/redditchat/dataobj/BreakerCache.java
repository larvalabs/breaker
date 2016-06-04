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

    private static HashMap<String, ArrayList<JsonMessage>> messageCache = new HashMap<String, ArrayList<JsonMessage>>();
    private static LRUMap<String, JsonUser> userCache = new LRUMap<>(1000);
    private static LRUMap<String, JsonChatRoom> roomCache = new LRUMap<>(1000);

    private static final String KEY_MESSAGES = "messages-";
    private static final String KEY_USER = "user-";

    private static String getMessagesKey(String roomName) {
        return KEY_MESSAGES + roomName;
    }

    public static void clearMessagesCache(String roomName) {
        synchronized (messageCache) {
            messageCache.remove(getMessagesKey(roomName));
        }
        Logger.info("Clear message cache for " + roomName);
    }

    public static ArrayList<JsonMessage> getLastMessages(ChatRoom room) {
        synchronized (messageCache) {
            ArrayList<JsonMessage> roomMessages = messageCache.get(getMessagesKey(room.getName()));
            if (roomMessages == null || !CACHE_ENABLED) {
                Logger.info("Cache miss room messages for " + room.getName());
                roomMessages = getLastJsonMessages(room);
                messageCache.put(getMessagesKey(room.getName()), roomMessages);
            } else {
//                Logger.info("Cache hit room messages for " + room.getName());
            }
            return roomMessages;
        }
    }

    public static void preloadCacheForRoom(ChatRoom room) {
        ArrayList<JsonMessage> lastJsonMessages = getLastJsonMessages(room);
        synchronized (messageCache) {
            messageCache.put(getMessagesKey(room.getName()), lastJsonMessages);
        }
    }

    private static ArrayList<JsonMessage> getLastJsonMessages(ChatRoom room) {
        ArrayList<JsonMessage> roomMessages = new ArrayList<JsonMessage>();
        List<Message> messageList = room.getMessages(Constants.DEFAULT_MESSAGE_LIMIT);
        for (Message message : messageList) {
            JsonMessage jsonMessage = JsonMessage.from(message, message.getUser().getUsername(), room.getName());
            if (message.isHasLinks()) {
                jsonMessage.setLinkInfo(message.getLinks());
            }
            roomMessages.add(jsonMessage);
        }
        Collections.reverse(roomMessages);
        return roomMessages;
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
            clearMessagesCache(event.room.name);
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
