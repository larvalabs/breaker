package com.larvalabs.redditchat.dataobj;

import com.larvalabs.redditchat.services.ActiveRoomsService;

import java.util.*;

public class JsonActiveRoomsUtil {
    public static HashMap<String, JsonActiveChatRoom> getActiveRooms(Long userId) {
        HashMap<String, JsonActiveChatRoom> activeRoomsState = new HashMap<>();
        List<JsonActiveChatRoom> activeRoomsList = ActiveRoomsService.getActiveRooms();

        activeRoomsState = updateRoomsToState(activeRoomsState, activeRoomsList);
        int offset = getLastRank(0, activeRoomsList);

        List<JsonActiveChatRoom> additionalRooms = new ArrayList<>();

        while(activeRoomsState.size() < 5 &&
                (additionalRooms = ActiveRoomsService.findMostActiveRooms(5-activeRoomsList.size(), offset, userId)).size() > 0) {

            activeRoomsState = updateRoomsToState(activeRoomsState, additionalRooms);
            offset = getLastRank(offset, additionalRooms);
        }

        return activeRoomsState;
    }

    private static HashMap<String, JsonActiveChatRoom> updateRoomsToState(HashMap<String, JsonActiveChatRoom> state, List<JsonActiveChatRoom> rooms) {
        Collections.sort(rooms, new Comparator<JsonActiveChatRoom>() {
            @Override
            public int compare(JsonActiveChatRoom room1, JsonActiveChatRoom room2) {
                return room1.getRank() - room2.getRank();
            }
        });


        for(JsonActiveChatRoom room : rooms) {
            if(!state.containsKey(room.getName())){
                state.put(room.getName(), room);
                if(state.size() >= 5) return state;
            }
        }

        return state;
    }

    private static int getLastRank(int offset, List<JsonActiveChatRoom> list) {
        for(JsonActiveChatRoom room : list) {
            if(offset < room.getRank()) { offset = room.getRank(); }
        }

        return offset;
    }
}
