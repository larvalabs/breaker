package com.larvalabs.redditchat.dataobj;

import com.larvalabs.redditchat.Constants;
import com.larvalabs.redditchat.realtime.ChatRoomStream;
import models.ChatRoom;
import models.ChatUser;
import models.Message;
import play.Logger;
import play.cache.Cache;

import java.util.*;

/**
 * Created by matt on 4/26/16.
 */
public class BreakerCache {

    public static final boolean CACHE_ENABLED = true;

    private static HashMap<String, ArrayList<JsonMessage>> messageCache = new HashMap<String, ArrayList<JsonMessage>>();
    private static HashMap<String, ArrayList<JsonUser>> userCache = new HashMap<String, ArrayList<JsonUser>>();

    private static final String KEY_MESSAGES = "messages-";
    private static final String KEY_USER = "user-";

    private static String getMessagesKey(String roomName) {
        return KEY_MESSAGES + roomName;
    }

    public static void clearMessagesCache(String roomName) {
        messageCache.remove(getMessagesKey(roomName));
        Logger.info("Clear message cache for " + roomName);
    }

    public static ArrayList<JsonMessage> getLastMessages(ChatRoom room) {
        ArrayList<JsonMessage> roomMessages = messageCache.get(getMessagesKey(room.getName()));
        if (roomMessages == null || !CACHE_ENABLED) {
            Logger.info("Cache miss room messages for " + room.getName());
            roomMessages = new ArrayList<JsonMessage>();
            List<Message> messageList = room.getMessages(Constants.DEFAULT_MESSAGE_LIMIT);
            for (Message message : messageList) {
                JsonMessage jsonMessage = JsonMessage.from(message, message.getUser().getUsername(), room.getName());
                if (message.isHasLinks()) {
                    jsonMessage.setLinkInfo(message.getLinks());
                }
                roomMessages.add(jsonMessage);
            }
            Collections.reverse(roomMessages);
            messageCache.put(getMessagesKey(room.getName()), roomMessages);
        } else {
            Logger.info("Cache hit room messages for " + room.getName());
        }
        return roomMessages;
    }

    public static void clearUsersCacheAll() {
        Logger.info("Clearing all user cache values.");
        userCache.clear();
    }

    public static void clearUsersCache(String roomName) {
        userCache.remove(roomName);
    }

    public static ArrayList<JsonUser> getUsersForRoom(ChatRoom room) {
        ArrayList<JsonUser> usersForRoom = userCache.get(room.getName());
        if (usersForRoom == null || !CACHE_ENABLED) {
            Logger.info("Cache miss userlist for " + room.getName());
            List<ChatUser> roomUsers = room.getUsers();
            TreeSet<String> usernamesPresent = room.getUsernamesPresent();
            usersForRoom = new ArrayList<JsonUser>();
            for (ChatUser roomUser : roomUsers) {
                JsonUser jsonUser = JsonUser.fromUser(roomUser, usernamesPresent.contains(roomUser.getUsername()));
                usersForRoom.add(jsonUser);
            }
            userCache.put(room.getName(), usersForRoom);
        } else {
            Logger.info("Cache hit userlist for " + room.getName());
        }
        return usersForRoom;
    }

    public static void handleEvent(ChatRoomStream.Event event) {
        if (event instanceof ChatRoomStream.Message) {
            ChatRoomStream.Message messageEvent = (ChatRoomStream.Message) event;
            clearMessagesCache(messageEvent.room.name);
        } else if (event instanceof ChatRoomStream.Join) {
            ChatRoomStream.Join joinEvent = (ChatRoomStream.Join) event;
            if (userCache.containsKey(joinEvent.room.name)) {
                ArrayList<JsonUser> jsonUsers = userCache.get(joinEvent.room.name);
                boolean found = false;
                for (JsonUser jsonUser : jsonUsers) {
                    if (jsonUser.username.equals(joinEvent.user.username)) {
                        found = true;
                        break;
                    }
                }

                if (!found) {
                    Logger.info("Clearing room member cache because new user joined: " + joinEvent.user.username);
                    clearUsersCache(joinEvent.room.name);
                }
            }
        }
    }

}
