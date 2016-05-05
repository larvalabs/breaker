package com.larvalabs.redditchat.dataobj;

import com.larvalabs.redditchat.Constants;
import com.sun.istack.internal.Nullable;
import controllers.Application;
import models.ChatRoom;
import models.ChatUser;

import javax.validation.constraints.Null;
import java.io.Serializable;
import java.util.*;
import java.util.Date;
import java.util.List;

public class JsonUser implements Serializable {

    public long id;
    public String username;
    public String notificationPreference;
    public Date lastSeen;
    public long lastSeenLongUTC;
    public long totalLikes;     // Likes this user has received on messages
    public String profileImageUrl;
    public String statusMessage;
    public boolean bot;

    public HashMap<String, JsonFlair> flair = new HashMap<>();

    // Optionally filled
    public String accessToken;
    public boolean online;

    public JsonUser(long id, String username, String notificationPreference, Date lastSeen,
                    long lastSeenLongUTC, long totalLikes, String profileImageUrl, String statusMessage, boolean bot,
                    boolean online, HashMap<String, JsonFlair> flair) {
        this.id = id;
        this.username = username;
        this.notificationPreference = notificationPreference;
        this.lastSeen = lastSeen;
        this.lastSeenLongUTC = lastSeenLongUTC;
        this.totalLikes = totalLikes;
        this.profileImageUrl = profileImageUrl;
        this.statusMessage = statusMessage;
        this.bot = bot;
        this.online = online;
        this.flair = flair;
    }

    /**
     * Load a user and set online status. Note: this loads user flair which can be expensive.
     * @param user
     * @param isOnline
     * @return
     */
    public static JsonUser fromUser(ChatUser user, boolean isOnline) {
        return new JsonUser(user.getId(), user.username, user.notificationPreference,
                user.getLastSeenDate(), user.getLastSeenDate().getTime(),
                user.getLikeCount(), user.getProfileImageUrl(), user.getStatusMessage(), user.isBot(),
                isOnline, user.getFlairAsJson());
    }

    /**
     *
     * @param user
     * @param isOnline
     * @return
     */
    public static JsonUser fromUserNoFlairLoad(ChatUser user, boolean isOnline) {
        return new JsonUser(user.getId(), user.username, user.notificationPreference,
                user.getLastSeenDate(), user.getLastSeenDate().getTime(),
                user.getLikeCount(), user.getProfileImageUrl(), user.getStatusMessage(), user.isBot(),
                isOnline, null);
    }

    public void addFlair(String roomName, JsonFlair flairObj) {
        if (flair == null) {
            flair = new HashMap<>();
        }
        flair.put(roomName, flairObj);
    }

    public ChatUser loadModelFromDatabase() {
        return ChatUser.findById(id);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        JsonUser jsonUser = (JsonUser) o;

        return id == jsonUser.id;

    }

    @Override
    public int hashCode() {
        return (int) (id ^ (id >>> 32));
    }
}
