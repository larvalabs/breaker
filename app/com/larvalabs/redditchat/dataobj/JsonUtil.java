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

        public TreeMap<String, JsonChatRoom> rooms = new TreeMap<>(lowerStringComparator);
        public TreeMap<String, JsonUser> allUsers = new TreeMap<>(lowerStringComparator);
        public TreeMap<String, JsonRoomMembers> members = new TreeMap<>(lowerStringComparator);
        public TreeMap<String, ArrayList<JsonMessage>> messages = new TreeMap<>(lowerStringComparator);
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
            JsonUser jsonUser = state.allUsers.get(thisUser.getUsername());
            if (jsonUser == null) {
                jsonUser = JsonUser.fromUserNoFlairLoad(thisUser, usernamesPresent.contains(thisUser.getUsername()));
                state.allUsers.put(jsonUser.username, jsonUser);
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

            if (!state.messages.containsKey(thisRoom.getName())) {
                long messagesStart = System.currentTimeMillis();
                ArrayList<JsonMessage> roomMessages = BreakerCache.getLastMessages(thisRoom);
                state.messages.put(thisRoom.getName(), roomMessages);
                Logger.info("Messages load time: " + (System.currentTimeMillis() - messagesStart));
            }
        }

        return state;
    }
}
