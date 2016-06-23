package com.larvalabs.redditchat.services;

import com.larvalabs.redditchat.Constants;
import com.larvalabs.redditchat.dataobj.JsonActiveChatRoom;
import com.larvalabs.redditchat.util.ActiveRoomsRedisUtil;
import play.db.jpa.JPA;
import play.modules.redis.Redis;

import javax.persistence.Query;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ActiveRoomsService {

    public static List<JsonActiveChatRoom> getActiveRooms() {
        List<JsonActiveChatRoom> activeRoomsList = ActiveRoomsRedisUtil.getActiveRooms();

        /* Not cached in Redis => read from DB and cache result */
        if(activeRoomsList == null || activeRoomsList.size() <= 0) {
            activeRoomsList = findMostActiveRooms(30);
            ActiveRoomsRedisUtil.cacheActiveRooms(activeRoomsList);
        }

        return activeRoomsList;
    }

    public static List<JsonActiveChatRoom> findMostActiveRooms(int limit) {
        return findMostActiveRooms(limit, 0, null);
    }

    public static List<JsonActiveChatRoom> findMostActiveRooms(int limit, int offset, Long userId) {
        StringBuilder sb = new StringBuilder();

        sb.append("SELECT id, name, displayName, iconUrl, activeUsers, rank ")
          .append("FROM ( ")
            .append("SELECT cr.id AS id, cr.name AS name, cr.displayname AS displayName, cr.iconurl AS iconUrl, COUNT(DISTINCT user_id) AS activeUsers, RANK() OVER (ORDER BY COUNT(DISTINCT user_id) DESC, name) as rank ")
            .append("FROM chatroom cr ")
            .append("LEFT JOIN message m on cr.id = m.room_id ")
            .append("WHERE m.createdate > (current_timestamp - interval '30 days') AND ")
            .append("m.user_id <> (SELECT id FROM chatuser WHERE username = :botname) ")
            .append("GROUP BY cr.id, cr.name, cr.displayname, cr.iconurl ")
            .append("ORDER BY activeUsers DESC ")
          .append(") AS ranked ")
          .append("WHERE name <> 'breakerapp' AND id NOT IN ( ")
            .append("SELECT room_id ")
            .append("FROM userroom ")
            .append("WHERE user_id = :userId ")
            .append(") ")
          .append("LIMIT :limit ")
          .append("OFFSET :offset");

        Query activeRoomsQuery = JPA.em().createNativeQuery(sb.toString());
        activeRoomsQuery.setParameter("limit", limit);
        if (offset > 0) {
            offset -= -1;
        } else if (offset < 0) {
            offset = 0;
        }
        activeRoomsQuery.setParameter("offset", offset);
        userId = (userId != null) ? userId : -1;
        activeRoomsQuery.setParameter("userId", userId);
        activeRoomsQuery.setParameter("botname", Constants.BREAKER_BOT_USERNAME);

        return convert(activeRoomsQuery.getResultList());
    }

    private static List<JsonActiveChatRoom> convert(List<Object[]> activeRoomsObjects) {
        List<JsonActiveChatRoom> activeRooms = new ArrayList<>();
        for(Object[] room : activeRoomsObjects) {
            activeRooms.add(new JsonActiveChatRoom(Integer.parseInt(room[0].toString()), room[1].toString(), room[2].toString(), (room[3] != null) ? room[3].toString() : "", Integer.parseInt(room[4].toString()),Integer.parseInt(room[5].toString())));
        }

        return activeRooms;
    }
}