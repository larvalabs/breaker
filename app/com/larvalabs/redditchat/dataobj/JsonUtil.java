package com.larvalabs.redditchat.dataobj;

import com.larvalabs.redditchat.Constants;
import com.larvalabs.redditchat.services.ActiveRoomsService;
import com.larvalabs.redditchat.util.RedisUtil;
import com.larvalabs.redditchat.util.Stats;
import models.ChatRoom;
import models.ChatUser;
import models.ChatUserRoomJoin;
import play.cache.Cache;
import play.modules.redis.Redis;

import java.util.*;

/**
 * Created by matt on 5/2/16.
 */
public class JsonUtil {

    public static class FullState {
        transient Comparator<String> lowerStringComparator = new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                if (o1 == o2) {
                    return 0;
                }
                if (o1 == null) {
                    return 1;
                }
                if (o2 == null) {
                    return -1;
                }
                return o1.toLowerCase().compareTo(o2.toLowerCase());
            }
        };

        transient Comparator<JsonMessage> messageTimeComparator = new Comparator<JsonMessage>() {
            @Override
            public int compare(JsonMessage o1, JsonMessage o2) {
                return new Long(o1.createDateLongUTC).compareTo(o2.createDateLongUTC);
            }
        };

        public TreeMap<String, JsonChatRoom> rooms = new TreeMap<>(lowerStringComparator);
        public HashMap<String, JsonActiveChatRoom> activeRooms = new HashMap<>();
        public TreeMap<String, JsonUser> users = new TreeMap<>(lowerStringComparator);
        public TreeMap<String, JsonRoomMembers> members = new TreeMap<>(lowerStringComparator);
        public TreeMap<String, ArrayList<String>> roomMessages = new TreeMap<>(lowerStringComparator);
        public HashMap<String, JsonMessage> messages = new HashMap<String, JsonMessage>();
        public HashMap<String, Long> lastSeenTimes = new HashMap<>();
    }

    public static FullState loadFullStateForUser(ChatUser user) {
        long startTime = System.currentTimeMillis();
        FullState state = new FullState();

        List<JsonActiveChatRoom> activeRooms = ActiveRoomsService.getActiveRooms(30);
        HashMap<String, JsonActiveChatRoom> activeRoomMap = new HashMap<>();
        for (JsonActiveChatRoom activeRoom : activeRooms) {
            activeRoomMap.put(activeRoom.getName(), activeRoom);
        }
        state.activeRooms = activeRoomMap;

        List<ChatUserRoomJoin> resultList = ChatUserRoomJoin.findWithoutDeletedAndUnopened(user);

        TreeSet<String> usernamesPresent = RedisUtil.getAllOnlineUsersForAllRooms();
        usernamesPresent.add(user.getUsername());   // Make sure user we're preloading it marked as online
        for (ChatUserRoomJoin chatRoomJoin : resultList) {
            ChatRoom thisRoom = chatRoomJoin.getRoom();
            ChatUser thisUser = chatRoomJoin.getUser();
//            Logger.info("Preload " + thisRoom.getName() + " for " + thisUser.getUsername());
            String roomName = thisRoom.getName();
            JsonChatRoom jsonChatRoom = state.rooms.get(roomName);
            if (jsonChatRoom == null) {
                jsonChatRoom = JsonChatRoom.from(thisRoom);
                state.rooms.put(roomName, jsonChatRoom);
            }

            JsonRoomMembers roomMembers = state.members.get(roomName);
            if (roomMembers == null) {
                roomMembers = new JsonRoomMembers();
                state.members.put(roomName.toLowerCase(), roomMembers);
            }
            JsonUser jsonUser = state.users.get(thisUser.getUsername());
            if (jsonUser == null) {
                jsonUser = JsonUser.fromUserNoFlairLoad(thisUser, usernamesPresent.contains(thisUser.getUsername()));
                state.users.put(jsonUser.username, jsonUser);
            }
            jsonUser.addFlair(roomName.toLowerCase(), new JsonFlair(chatRoomJoin.getFlairText(), chatRoomJoin.getFlairCss(), chatRoomJoin.getFlairPosition()));
            if (jsonChatRoom.isModerator(thisUser.getUsername())) {
                roomMembers.mods.add(jsonUser.username);
            } else if (jsonUser.online) {
                roomMembers.online.add(jsonUser.username);
            } else {
                roomMembers.offline.add(jsonUser.username);
            }
            state.members.put(roomName, roomMembers);

            if (!state.roomMessages.containsKey(roomName)) {
//                long messagesStart = System.currentTimeMillis();
                TreeSet<JsonMessage> roomMessages = BreakerCache.getLastMessages(thisRoom);
                ArrayList<String> messageIds = new ArrayList<>();
                for (JsonMessage roomMessage : roomMessages) {
                    if (!roomMessage.deleted) {
                        state.messages.put(roomMessage.uuid, roomMessage);
                        messageIds.add(roomMessage.uuid);
                    }
                }
                state.roomMessages.put(roomName, messageIds);
//                Logger.info("Messages load time: " + (System.currentTimeMillis() - messagesStart));
            }

            if (user.equals(thisUser)) {
                state.lastSeenTimes.put(roomName, chatRoomJoin.getLastSeenMessageTime());
            }

        }

        Stats.measure(Stats.StatKey.LOAD_FULLSTATE_TIME, (System.currentTimeMillis() - startTime));

        return state;
    }

}
