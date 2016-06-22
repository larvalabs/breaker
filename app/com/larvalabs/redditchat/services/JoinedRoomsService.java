package com.larvalabs.redditchat.services;

import models.ChatUser;
import models.ChatUserRoomJoin;
import play.db.jpa.JPA;

import javax.persistence.Query;
import java.util.List;

public class JoinedRoomsService {
    public static List<ChatUserRoomJoin> findJoinedRooms(ChatUser user) {
        Query getAllStuffQuery = JPA.em().createQuery("select ur from ChatUserRoomJoin ur join fetch ur.room urr join fetch ur.user u where ur.room " +
                "in (select room from ChatUserRoomJoin ur2 where ur2.user = :user) " +
                "and ur.room.deleted = false and ur.room.open = true")
                .setParameter("user", user);
         return getAllStuffQuery.getResultList();
    }
}
