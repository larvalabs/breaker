package com.larvalabs.redditchat.dataobj;

import com.larvalabs.redditchat.services.ActiveRoomsService;
import com.larvalabs.redditchat.services.JoinedRoomsService;
import models.ChatUser;
import models.ChatUserRoomJoin;

import java.util.*;

public class JsonActiveRoomsUtil {
    public static HashMap<String, JsonActiveChatRoom> getActiveRooms(ChatUser user, int offset, int amount) {
        HashMap<String, JsonActiveChatRoom> activeRoomsState = new HashMap<>();
        List<ChatUserRoomJoin> joinedUserRoom = JoinedRoomsService.findJoinedRooms(user);

        List<JsonActiveChatRoom> activeRoomsList = new ArrayList<>();

        if (offset < 30) {
            activeRoomsList = ActiveRoomsService.getActiveRooms(); // load top 30
            activeRoomsState = updateRoomsToState(activeRoomsState, activeRoomsList, joinedUserRoom, offset, amount);
            offset = getLastRank(offset, activeRoomsList);
        }

        List<JsonActiveChatRoom> additionalRooms = new ArrayList<>();

        while (activeRoomsState.size() < amount &&
                (additionalRooms = ActiveRoomsService.findMostActiveRooms(amount - activeRoomsState.size(), offset-1, user.getId())).size() > 0) {

            activeRoomsState = updateRoomsToState(activeRoomsState, additionalRooms, joinedUserRoom, offset, amount);
            offset = getLastRank(offset, additionalRooms);
        }

        return activeRoomsState;
    }

    private static HashMap<String, JsonActiveChatRoom> updateRoomsToState(HashMap<String, JsonActiveChatRoom> state, List<JsonActiveChatRoom> rooms, List<ChatUserRoomJoin> joinedUserRoom, int offset, int amount) {
        Collections.sort(rooms, new Comparator<JsonActiveChatRoom>() {
            @Override
            public int compare(JsonActiveChatRoom room1, JsonActiveChatRoom room2) {
                return room1.getRank() - room2.getRank();
            }
        });

        for (JsonActiveChatRoom room : rooms) {
            if (room.getRank() > offset && !state.containsKey(room.getName()) && !isJoinedRoom(joinedUserRoom, room)) {
                state.put(room.getName(), room);
                if (state.size() >= amount) return state;
            }
        }

        return state;
    }

    private static int getLastRank(int offset, List<JsonActiveChatRoom> list) {
        for (JsonActiveChatRoom room : list) {
            if (offset < room.getRank()) {
                offset = room.getRank();
            }
        }

        return offset;
    }

    private static boolean isJoinedRoom(List<ChatUserRoomJoin> joinedUserRoom, JsonActiveChatRoom room) {
        for (ChatUserRoomJoin joinedRoom : joinedUserRoom) {
            if (joinedRoom.getRoom().getName().equals(room.getName())) {
                return true;
            }
        }
        return false;
    }
}
