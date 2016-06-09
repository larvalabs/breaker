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

    public List<JsonActiveChatRoom> findMostActiveRooms(int limit, int offset) {
        StringBuilder sb = new StringBuilder();

        sb.append("SELECT cr.name AS name, cr.displayname AS displayName, cr.iconurl AS iconUrl, COUNT(DISTINCT user_id) AS activeUsers FROM chatroom cr ");
        sb.append("LEFT JOIN message m on cr.id = m.room_id ");
        sb.append("WHERE m.createdate > (current_timestamp - interval '30 days') ");
        sb.append("GROUP BY cr.name, cr.displayname, cr.iconurl ");
        sb.append("ORDER BY activeUsers desc ");
        sb.append("LIMIT :limit ");
        sb.append("OFFSET :offset");

        Query activeRoomsQuery = JPA.em().createNativeQuery(sb.toString());
        activeRoomsQuery.setParameter("limit", limit);
        activeRoomsQuery.setParameter("offset", offset);

        return convert(activeRoomsQuery.getResultList());
    }

    private List<JsonActiveChatRoom> convert(List<Object[]> activeRoomsObjects) {
        List<JsonActiveChatRoom> activeRooms = new ArrayList<>();
        for(Object[] room : activeRoomsObjects) {
            activeRooms.add(new JsonActiveChatRoom(room[0].toString(), room[1].toString(), (room[2] != null) ? room[2].toString() : "", Integer.parseInt(room[3].toString())));
        }

        return activeRooms;
    }
}