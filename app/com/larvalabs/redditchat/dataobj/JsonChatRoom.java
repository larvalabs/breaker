package com.larvalabs.redditchat.dataobj;

import models.ChatRoom;
import models.ChatUser;
import models.ChatUserRoomJoin;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: matt
 * Date: 4/22/15
 * Time: 11:59 AM
 * To change this template use File | Settings | File Templates.
 */
public class JsonChatRoom implements Serializable {

    public long id;
    public String name;
    public long numberUsers;
    public long numberNewMessagesForUser;
    public boolean starred;
    public long liveUserCount;
    public String iconUrl;
    public int iconUrlSource;
    public boolean noIconAvailableFromStore;
    public String banner;
    public boolean isUserModerator;

    public boolean watching;

    public long lastSeenMessageTime;
    public String flairScale;
    public JsonRoomStyles styles;

    // Filled when getting messages
    public String[] usernamesPresent;
    public JsonMessage[] messages;

    public JsonChatRoom(long id, String name, long numberUsers,
                        long numberNewMessagesForUser, boolean starred, long liveUserCount, String iconUrl, int iconUrlSource, boolean noIconAvailableFromStore,
                        String banner, boolean isUserModerator, long lastSeenMessageTime, String flairScale, JsonRoomStyles jsonRoomStyles) {
        this.id = id;
        this.name = name;
        this.numberUsers = numberUsers;
        this.numberNewMessagesForUser = numberNewMessagesForUser;
        this.starred = starred;
        this.liveUserCount = liveUserCount;
        this.iconUrl = iconUrl;
        this.iconUrlSource = iconUrlSource;
        this.noIconAvailableFromStore = noIconAvailableFromStore;
        this.banner = banner;
        this.isUserModerator = isUserModerator;
        this.lastSeenMessageTime = lastSeenMessageTime;
        this.flairScale = flairScale;
        this.styles = jsonRoomStyles;
    }

    /**
     * Get a json rep for this room but doens't load unread counts or starred status for logged in user.
     * @param room
     * @return
     */
    public static JsonChatRoom from(ChatRoom room) {
        JsonRoomStyles jsonRoomStyles = new JsonRoomStyles(room.sidebarBackgroundColor, room.sidebarTextColor,
                room.sidebarRoomSelectedColor, room.sidebarRoomHoverColor, room.sidebarRoomTextColor,
                room.sidebarUnreadColor, room.sidebarUnreadTextColor, room.signinButtonColor,
                room.signinButtonTextColor);

        JsonChatRoom jsonChatRoom = new JsonChatRoom(room.getId(), room.name,
                room.numberOfUsers,
                0, false, room.getCurrentUserCount(), room.getIconUrl(),
                room.getIconUrlSource(), room.isNoIconAvailableFromStore(), room.getBanner(), false,
                0, room.flairScale, jsonRoomStyles);
        return jsonChatRoom;
    }

    public static JsonChatRoom from(ChatRoom room, ChatUser loggedInUser) {
        return from(room, loggedInUser, null, false);
    }

    public static JsonChatRoom from(ChatRoom room, ChatUser loggedInUser, HashMap<Long, Long> unreadCountsByRoomId, boolean loadWatcherStatus) {
        Long unreadCount = 0l;
        if (unreadCountsByRoomId != null && unreadCountsByRoomId.containsKey(room.id)) {
            unreadCount = unreadCountsByRoomId.get(room.id);
        }
        ChatUserRoomJoin join = ChatUserRoomJoin.findByUserAndRoom(loggedInUser, room);

        JsonRoomStyles jsonRoomStyles = new JsonRoomStyles(room.sidebarBackgroundColor, room.sidebarTextColor,
                room.sidebarRoomSelectedColor, room.sidebarRoomHoverColor, room.sidebarRoomTextColor,
                room.sidebarUnreadColor, room.sidebarUnreadTextColor, room.signinButtonColor,
                room.signinButtonTextColor);

        JsonChatRoom jsonChatRoom = new JsonChatRoom(room.getId(), room.name,
                room.numberOfUsers,
                unreadCount, loggedInUser.isRoomStarred(room), room.getCurrentUserCount(), room.getIconUrl(),
                room.getIconUrlSource(), room.isNoIconAvailableFromStore(), room.getBanner(), room.isModerator(loggedInUser),
                join.getLastSeenMessageTime(), room.flairScale, jsonRoomStyles);
        if (loadWatcherStatus) {
            jsonChatRoom.watching = room.getWatchers().contains(loggedInUser);
        }
        return jsonChatRoom;
    }

    public static JsonChatRoom[] convert(ChatUser loggedInUser, List<ChatRoom> roomList, HashMap<Long, Long> unreadCountsByRoomId, boolean loadWatcherStatus) {
        ArrayList<JsonChatRoom> jsonList = new ArrayList<JsonChatRoom>();
        for (ChatRoom chatRoom : roomList) {
            jsonList.add(JsonChatRoom.from(chatRoom, loggedInUser, unreadCountsByRoomId, loadWatcherStatus));
        }
        return jsonList.toArray(new JsonChatRoom[]{});
    }
}
