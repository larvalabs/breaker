package com.larvalabs.redditchat.dataobj;

import java.io.Serializable;

public final class JsonActiveChatRoom implements Serializable{
    private final String name;
    private final String displayName;
    private final String iconUrl;
    private final int activeUsers;

    public JsonActiveChatRoom(String roomName, String displayName, String iconUrl, int activeUsers) {
        this.name = roomName;
        this.displayName = displayName;
        this.iconUrl = iconUrl;
        this.activeUsers = activeUsers;
    }

    public String getName() {
        return name;
    }

    public String getDisplayName() { return displayName; }

    public String getIconUrl() { return iconUrl; }

    public int getActiveUsers() {
        return activeUsers;
    }

}