package com.larvalabs.redditchat.dataobj;

import com.larvalabs.redditchat.Constants;
import controllers.Application;
import models.ChatRoom;
import models.ChatUser;
import play.Logger;

import java.util.*;

public class JsonUser {

    public long id;
    public String username;
    public String notificationPreference;
    public Date lastSeen;
    public long lastSeenLongUTC;
    public long totalLikes;     // Likes this user has received on messages
    public String profileImageUrl;
    public String statusMessage;
    public boolean bot;

    // Optionally filled
    public String accessToken;
    public boolean online;
    // Filled in when part of a user list for a room
    public boolean modForRoom;

    // Filled in when requesting full user profile
    public JsonMessage[] recentMessages;
    public JsonChatRoom[] topChatRooms;
    public JsonMessage[] mentions;  // Filled in if user requesting = user requested

    // Additional role information for rendering badges
    public static final int ROLE_NONE = 0;
    public static final int ROLE_DEVELOPER_THIS = 1;
    public static final int ROLE_DEVELOPER_OTHER = 2;
    public int roleForRoom = ROLE_NONE;
    public String flairForRoom = "";
    public JsonChatRoom[] developerForRooms;

    public JsonUser(long id, String username, String notificationPreference, Date lastSeen,
                    long lastSeenLongUTC, long totalLikes, String profileImageUrl, String statusMessage, boolean bot) {
        this.id = id;
        this.username = username;
        this.notificationPreference = notificationPreference;
        this.lastSeen = lastSeen;
        this.lastSeenLongUTC = lastSeenLongUTC;
        this.totalLikes = totalLikes;
        this.profileImageUrl = profileImageUrl;
        this.statusMessage = statusMessage;
        this.bot = bot;
    }

    public static JsonUser fromUser(ChatUser user) {
        return new JsonUser(user.getId(), user.username, user.notificationPreference,
                user.getLastSeenDate(), user.getLastSeenDate().getTime(),
                user.getLikeCount(), user.getProfileImageUrl(), user.getStatusMessage(), user.isBot());
    }

    /**
     * Adds information to user specific to a chat room, mainly online status and moderator flag.
     * @param user
     * @param room
     * @return
     */
    public static JsonUser fromUserForRoom(ChatUser user, ChatRoom room) {
        JsonUser jsonUser = fromUser(user);
        jsonUser.online = room.isUserPresent(user);
        jsonUser.modForRoom = room.isModerator(user);
        return jsonUser;
    }

    public static JsonUser fromUserWithFullDetails(ChatUser user, ChatUser loggedInUser) {
        JsonUser jsonUser = fromUser(user);
        List<ChatRoom> topChatRoomsList = user.getTopChatRooms(10);
        jsonUser.topChatRooms = JsonChatRoom.convert(loggedInUser, topChatRoomsList, null, false);
        jsonUser.recentMessages = JsonMessage.convert(user.getLatestMessages(Constants.DEFAULT_MESSAGE_LIMIT), loggedInUser, JsonMessage.ListType.RECENT_MESSAGES);
        if (user.equals(loggedInUser)) {
            jsonUser.mentions = JsonMessage.convert(user.getMentioned(20), user, JsonMessage.ListType.MENTIONS);
        }
        return jsonUser;
    }

    // todo Convert this role stuff over to visitor, user, moderator
/*
    public void fillRoleInfo(ChatUser messagePostingUser, ChatRoom room) {
        fillRoleInfo(messagePostingUser, room, null);
    }

    public void fillRoleInfo(ChatUser messagePostingUser, ChatRoom room, HashMap<ChatUser, Set<ChatRoom>> userDeveloperMap) {
//        LogUtil.info("fillRoleInfo for " + messagePostingUser.username + " in " + (room != null ? room.appPackage : "(no room)"));
        Set<ChatRoom> developerOfRooms;
        if (userDeveloperMap != null) {
            developerOfRooms = userDeveloperMap.get(messagePostingUser);
        } else {
            developerOfRooms = messagePostingUser.getDeveloperOfRooms();
        }
        if (developerOfRooms != null && developerOfRooms.size() > 0) {
            if (room == null) {
                roleForRoom = JsonUser.ROLE_NONE;
            } else if (room != null && developerOfRooms.size() > 0 && !developerOfRooms.contains(room)) {
                roleForRoom = JsonUser.ROLE_DEVELOPER_OTHER;
            } else {
                flairForRoom += Constants.Flair.DEV_SAME_ROOM.getAsString();
                roleForRoom = JsonUser.ROLE_DEVELOPER_THIS;
            }
            // todo Add this back in if we need it, but make it a bit faster
//            developerForRooms = JsonChatRoom.convert(messagePostingUser,
//                    new ArrayList<ChatRoom>(messagePostingUser.getDeveloperOfRooms()), null, false);
        }

        if (Stats.isTopGlobalStarredUser(messagePostingUser)) {
            flairForRoom += Constants.Flair.TOP_STARS_GLOBAL.getAsString();
        }
        if (room != null) {
            if (Stats.isTopStarredUserForRoom(messagePostingUser, room)) {
                flairForRoom += Constants.Flair.TOP_STARS_ROOM.getAsString();
            }
        }
//        LogUtil.info("DONE fillRoleInfo for " + messagePostingUser.username + " in " + (room != null ? room.appPackage : "(no room)"));
    }
*/
}
