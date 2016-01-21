package com.larvalabs.redditchat.dataobj;

import models.ChatUser;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

/**
 * Created by matt on 1/11/16.
 */
public class JsonUserSearch {

    public String roomName;
    public String query;
    public JsonUser[] onlineUsers;
    public JsonUser[] offlineUsers;

    public JsonUserSearch(String roomName, String query, JsonUser[] onlineUsers, JsonUser[] offlineUsers) {
        this.roomName = roomName;
        this.query = query;
        this.onlineUsers = onlineUsers;
        this.offlineUsers = offlineUsers;
    }

    public static JsonUserSearch make(String roomName, String query, List<ChatUser> usersForRoom, TreeSet<String> usernamesPresent) {
        ArrayList<ChatUser> usersToProcess = new ArrayList<ChatUser>(usersForRoom);
        ArrayList<JsonUser> online = new ArrayList<JsonUser>();
        ArrayList<JsonUser> offline = new ArrayList<JsonUser>();
        for (ChatUser user : usersToProcess) {
            if (user.username.toLowerCase().startsWith(query.toLowerCase())) {
                JsonUser jsonUser = JsonUser.fromUser(user);
                // Note: No longer separating into online/offline
                if (usernamesPresent.contains(user.getUsername()) && false) {
                    online.add(jsonUser);
                } else {
                    offline.add(jsonUser);
                }
            }
        }

        // todo sort lists

        return new JsonUserSearch(roomName, query, online.toArray(new JsonUser[]{}), offline.toArray(new JsonUser[]{}));
    }
}
