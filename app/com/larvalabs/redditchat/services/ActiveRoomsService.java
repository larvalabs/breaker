package com.larvalabs.redditchat.services;

import com.larvalabs.redditchat.dataobj.JsonActiveChatRoom;
import play.db.jpa.JPA;

import javax.persistence.Query;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ActiveRoomsService {
    private static ActiveRoomsService instance = null;

    protected ActiveRoomsService() {}

    public static ActiveRoomsService getInstance() {
        if(instance == null) {
            instance = new ActiveRoomsService();
        }
        return instance;
    }

    public List<JsonActiveChatRoom> findMostActiveRooms(int limit, int offset, Long userId) {
        StringBuilder sb = new StringBuilder();

        /*
            SELECT id, name, displayName, iconUrl, activeUsers, rank
            FROM (
                SELECT cr.id AS id, cr.name AS name, cr.displayname AS displayName, cr.iconurl AS iconUrl, COUNT(DISTINCT user_id) AS activeUsers, RANK() OVER (ORDER BY COUNT(DISTINCT user_id) DESC, name) as rank
                FROM chatroom cr
                LEFT JOIN message m on cr.id = m.room_id
                WHERE m.createdate > (current_timestamp - interval '30 days')
                GROUP BY cr.id, cr.name, cr.displayname, cr.iconurl
                ORDER BY activeUsers DESC
            ) AS ranked
            WHERE name <> 'breakerapp' AND id NOT IN (
                SELECT room_id
                FROM userroom
                WHERE user_id = 35
            )
            LIMIT 5
            OFFSET 0;
         */

        sb.append("SELECT id, name, displayName, iconUrl, activeUsers, rank ")
          .append("FROM ( ")
            .append("SELECT cr.id AS id, cr.name AS name, cr.displayname AS displayName, cr.iconurl AS iconUrl, COUNT(DISTINCT user_id) AS activeUsers, RANK() OVER (ORDER BY COUNT(DISTINCT user_id) DESC, name) as rank ")
            .append("FROM chatroom cr ")
            .append("LEFT JOIN message m on cr.id = m.room_id ")
            .append("WHERE m.createdate > (current_timestamp - interval '30 days') ")
            .append("GROUP BY cr.id, cr.name, cr.displayname, cr.iconurl ")
            .append("ORDER BY activeUsers DESC ")
          .append(") AS ranked ")
          .append("WHERE name <> 'breakerapp' AND id NOT IN ( ")
            .append("SELECT room_id ")
            .append("FROM userroom ");
        if(userId != null)
          sb.append("WHERE user_id = :userId ");
          sb.append(") ")
          .append("LIMIT :limit ")
          .append("OFFSET :offset");

        Query activeRoomsQuery = JPA.em().createNativeQuery(sb.toString());
        activeRoomsQuery.setParameter("limit", limit);
        activeRoomsQuery.setParameter("offset", offset);
        if(userId != null) activeRoomsQuery.setParameter("userId", userId);

        return convert(activeRoomsQuery.getResultList());
    }

    private List<JsonActiveChatRoom> convert(List<Object[]> activeRoomsObjects) {
        List<JsonActiveChatRoom> activeRooms = new ArrayList<>();
        for(Object[] room : activeRoomsObjects) {
            activeRooms.add(new JsonActiveChatRoom(Integer.parseInt(room[0].toString()), room[1].toString(), room[2].toString(), (room[3] != null) ? room[3].toString() : "", Integer.parseInt(room[4].toString()),Integer.parseInt(room[5].toString())));
        }

        return activeRooms;
    }
}