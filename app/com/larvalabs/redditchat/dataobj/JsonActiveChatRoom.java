package com.larvalabs.redditchat.dataobj;

import java.io.Serializable;

public final class JsonActiveChatRoom implements Serializable{
    private final int id;
    private final int rank;
    private final String name;
    private final String displayName;
    private final String iconUrl;
    private int activeUsers;

    public JsonActiveChatRoom(int id, String roomName, String displayName, String iconUrl, int activeUsers, int rank) {
        this.id = id;
        this.name = roomName;
        this.displayName = displayName;
        this.iconUrl = iconUrl;
        this.activeUsers = activeUsers;
        this.rank = rank;
    }

    public int getId() { return id; }

    public int getRank() { return rank; }

    public String getName() {
        return name;
    }

    public String getDisplayName() { return displayName; }

    public String getIconUrl() { return iconUrl; }

    public int getActiveUsers() {
        return activeUsers;
    }

    public void setActiveUsers(int activeUsers) {
        this.activeUsers = activeUsers;
    }
}