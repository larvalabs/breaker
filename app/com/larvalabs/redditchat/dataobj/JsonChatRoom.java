package com.larvalabs.redditchat.dataobj;

import com.larvalabs.redditchat.Constants;
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
    public long liveUserCount;
    public String iconUrl;
    public int iconUrlSource;
    public boolean noIconAvailableFromStore;
    public String banner;
    public List<String> moderators;

    public String flairScale;
    public boolean isPrivate;
    public JsonRoomStyles styles;

    // Filled when getting messages
    public String[] usernamesPresent;
    public JsonMessage[] messages;

    public JsonChatRoom(long id, String name, long numberUsers,
                        long liveUserCount, String iconUrl, int iconUrlSource, boolean noIconAvailableFromStore,
                        String banner, String flairScale, boolean isPrivate, JsonRoomStyles jsonRoomStyles,
                        List<String> moderators) {
        this.id = id;
        this.name = name;
        this.numberUsers = numberUsers;
        this.liveUserCount = liveUserCount;
        this.iconUrl = iconUrl;
        this.iconUrlSource = iconUrlSource;
        this.noIconAvailableFromStore = noIconAvailableFromStore;
        this.banner = banner;
        this.flairScale = flairScale;
        this.isPrivate = isPrivate;
        this.styles = jsonRoomStyles;
        this.moderators = moderators;
    }

    /**
     * Get a json rep for this room but doens't load unread counts or starred status for logged in user.
     * @param room
     * @return
     */
    public static JsonChatRoom from(ChatRoom room, List<String> moderators) {
        JsonRoomStyles jsonRoomStyles = new JsonRoomStyles(room.sidebarBackgroundColor, room.sidebarTextColor,
                room.sidebarRoomSelectedColor, room.sidebarRoomHoverColor, room.sidebarRoomTextColor,
                room.sidebarUnreadColor, room.sidebarUnreadTextColor, room.signinButtonColor,
                room.signinButtonTextColor);

        JsonChatRoom jsonChatRoom = new JsonChatRoom(room.getId(), room.name,
                room.numberOfUsers,
                room.getCurrentUserCount(), room.getIconUrl(),
                room.getIconUrlSource(), room.isNoIconAvailableFromStore(), room.getBanner(),
                room.flairScale, room.isPrivateRoom(), jsonRoomStyles, moderators);
        return jsonChatRoom;
    }

    public ChatRoom loadModelFromDatabase() {
        return ChatRoom.findById(id);
    }

    public boolean isDefaultRoom() {
        return name.equals(Constants.CHATROOM_DEFAULT);
    }
}
