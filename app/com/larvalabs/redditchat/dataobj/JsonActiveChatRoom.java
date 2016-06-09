package com.larvalabs.redditchat.dataobj;

import java.io.Serializable;

public final class JsonActiveChatRoom implements Serializable{
    private final String roomName;
    private final int activeUsers;

    public JsonActiveChatRoom(String roomName, int activeUsers) {
        this.roomName = roomName;
        this.activeUsers = activeUsers;
    }

    public String getRoomName() {
        return roomName;
    }

    public int getActiveUsers() {
        return activeUsers;
    }

}