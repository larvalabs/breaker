package com.larvalabs.redditchat.dataobj;

import models.ChatRoom;
import models.ChatUser;
import models.ChatUserRoomJoin;
import play.Logger;
import play.db.jpa.JPA;

import javax.persistence.Query;
import java.util.*;

/**
 * Created by matt on 5/2/16.
 */
public class JsonUtil {

    public static class FullState {
        Comparator<String> lowerStringComparator = new Comparator<String>() {
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

        Comparator<JsonMessage> messageTimeComparator = new Comparator<JsonMessage>() {
            @Override
            public int compare(JsonMessage o1, JsonMessage o2) {
                return new Long(o1.createDateLongUTC).compareTo(o2.createDateLongUTC);
            }
        };



        public TreeMap<String, JsonChatRoom> rooms = new TreeMap<>(lowerStringComparator);
        public TreeMap<String, JsonUser> users = new TreeMap<>(lowerStringComparator);
        public TreeMap<String, JsonRoomMembers> members = new TreeMap<>(lowerStringComparator);
        public TreeMap<String, ArrayList<String>> roomMessages = new TreeMap<>(lowerStringComparator);
        public HashMap<String, JsonMessage> messages = new HashMap<String, JsonMessage>();
        public HashMap<String, Long> lastSeenTimes = new HashMap<>();
    }

    public static FullState loadFullStateForUser(ChatUser user) {
        FullState state = new FullState();

        Query getAllStuffQuery = JPA.em().createQuery("select ur from ChatUserRoomJoin ur join fetch ur.room urr join fetch ur.user u where ur.room in (select room from ChatUserRoomJoin ur2 where ur2.user = :user)")
                .setParameter("user", user);
        List<ChatUserRoomJoin> resultList = getAllStuffQuery.getResultList();

        TreeSet<String> usernamesPresent = ChatRoom.getAllOnlineUsersForAllRooms();
        usernamesPresent.add(user.getUsername());   // Make sure user we're preloading it marked as online
        for (ChatUserRoomJoin chatRoomJoin : resultList) {
            ChatRoom thisRoom = chatRoomJoin.getRoom();
            ChatUser thisUser = chatRoomJoin.getUser();
//            Logger.info("Preload " + thisRoom.getName() + " for " + thisUser.getUsername());
            if (!state.rooms.containsKey(thisRoom.getName())) {
                state.rooms.put(thisRoom.getName(), JsonChatRoom.from(thisRoom, thisRoom.getModeratorUsernames()));
            }

            JsonRoomMembers roomMembers = state.members.get(thisRoom.getName());
            if (roomMembers == null) {
                roomMembers = new JsonRoomMembers();
                state.members.put(thisRoom.getName(), roomMembers);
            }
            JsonUser jsonUser = state.users.get(thisUser.getUsername());
            if (jsonUser == null) {
                jsonUser = JsonUser.fromUserNoFlairLoad(thisUser, usernamesPresent.contains(thisUser.getUsername()));
                state.users.put(jsonUser.username, jsonUser);
            }
            jsonUser.addFlair(thisRoom.getName(), new JsonFlair(chatRoomJoin.getFlairText(), chatRoomJoin.getFlairCss(), chatRoomJoin.getFlairPosition()));
            if (thisRoom.getModerators().contains(thisUser)) {  // check if slow
                roomMembers.mods.add(jsonUser.username);
            } else if (jsonUser.online) {
                roomMembers.online.add(jsonUser.username);
            } else {
                roomMembers.offline.add(jsonUser.username);
            }
            state.members.put(thisRoom.getName(), roomMembers);

            if (!state.roomMessages.containsKey(thisRoom.getName())) {
                long messagesStart = System.currentTimeMillis();
                ArrayList<JsonMessage> roomMessages = BreakerCache.getLastMessages(thisRoom);
                ArrayList<String> messageIds = new ArrayList<>();
                for (JsonMessage roomMessage : roomMessages) {
                    state.messages.put(roomMessage.uuid, roomMessage);
                    messageIds.add(roomMessage.uuid);
                }
                state.roomMessages.put(thisRoom.getName(), messageIds);
                Logger.info("Messages load time: " + (System.currentTimeMillis() - messagesStart));
            }

            if (user.equals(thisUser)) {
                state.lastSeenTimes.put(thisRoom.getName(), chatRoomJoin.getLastSeenMessageTime());
            }
        }

        return state;
    }
}
