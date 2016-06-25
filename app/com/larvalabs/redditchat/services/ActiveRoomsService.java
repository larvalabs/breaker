package com.larvalabs.redditchat.services;

import com.larvalabs.redditchat.dataobj.BreakerCache;
import com.larvalabs.redditchat.dataobj.JsonActiveChatRoom;
import com.larvalabs.redditchat.dataobj.JsonChatRoom;
import com.larvalabs.redditchat.util.RedisUtil;
import play.cache.Cache;

import java.util.*;

public class ActiveRoomsService {

    public static final String CACHEKEY_ACTIVEROOMS = "active_rooms_json";

    public static List<JsonActiveChatRoom> getActiveRooms(int limit) {
        List<JsonActiveChatRoom> activeRoomsList = (List<JsonActiveChatRoom>) Cache.get(CACHEKEY_ACTIVEROOMS);
        if (activeRoomsList == null) {
            TreeSet<String> activeRooms = RedisUtil.getActiveRooms(limit);
            activeRoomsList = new ArrayList<>();
            int rank = 1;
            for (String roomName : activeRooms) {
                JsonChatRoom jsonChatRoom = BreakerCache.getJsonChatRoom(roomName);
                if (jsonChatRoom != null) {
                    activeRoomsList.add(new JsonActiveChatRoom(0, roomName, jsonChatRoom.displayName, jsonChatRoom.iconUrl,
                            (int) RedisUtil.getCurrentUserCount(roomName), rank));
                    rank++;
                }
            }
            Cache.set(CACHEKEY_ACTIVEROOMS, activeRoomsList, "1min");
        }

        return activeRoomsList;
    }
}