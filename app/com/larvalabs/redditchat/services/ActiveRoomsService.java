package com.larvalabs.redditchat.services;

import com.larvalabs.redditchat.dataobj.BreakerCache;
import com.larvalabs.redditchat.dataobj.JsonActiveChatRoom;
import com.larvalabs.redditchat.dataobj.JsonChatRoom;
import com.larvalabs.redditchat.util.RedisUtil;

import java.util.*;

public class ActiveRoomsService {

    public static List<JsonActiveChatRoom> getActiveRooms(int limit) {
        TreeSet<String> activeRooms = RedisUtil.getActiveRooms(limit);
        List<JsonActiveChatRoom> activeRoomsList = new ArrayList<>();
        int rank = 1;
        for (String roomName : activeRooms) {
            JsonChatRoom jsonChatRoom = BreakerCache.getJsonChatRoom(roomName);
            if (jsonChatRoom != null) {
                activeRoomsList.add(new JsonActiveChatRoom(0, roomName, jsonChatRoom.displayName, jsonChatRoom.iconUrl, 1, rank));
                rank++;
            }
        }

        return activeRoomsList;
    }
}