package controllers;

import com.larvalabs.redditchat.util.TopAlert;
import models.ChatRoom;
import models.ChatUser;

/**
 * @author John Watkinson
 */
public class RoomManage extends PreloadUserController {

    public static void roomPrefs(String roomName) {
        ChatUser user = connected();
        ChatRoom chatRoom = ChatRoom.findByName(roomName);
        if (chatRoom == null) {
            error("Room '" + roomName + "' not found.");
        } else if (!chatRoom.isModerator(user)) {
            error("Not a moderator of '" + roomName + "'.");
        } else {
            render(chatRoom);
        }
    }

    public static void savePrefs(Long roomId, String banner, String iconUrl, Integer karmaThreshold) {
        ChatUser user = connected();
        ChatRoom chatRoom = ChatRoom.findById(roomId);
        if (!chatRoom.isModerator(user)) {
            error("Not a moderator of '" + chatRoom.getName() + "'.");
        } else {
            chatRoom.setBanner(banner);
            chatRoom.setIconUrl(iconUrl);
            chatRoom.setKarmaThreshold(karmaThreshold == null ? 0 : karmaThreshold);
            chatRoom.save();
            TopAlert alert = new TopAlert(TopAlert.Type.SUCCESS, "Room updated.", "Room information saved.");
            alert.toFlash(flash);
            WebSocket.room(chatRoom.getName());
        }
    }

    public static void saveStylePrefs(Long roomId, String sidebarBackgroundColor, String sidebarTextColor, String sidebarRoomSelectedColor,
                                      String sidebarRoomHoverColor, String sidebarRoomTextColor, String sidebarUnreadColor,
                                      String sidebarUnreadTextColor, String signinButtonColor, String signinButtonTextColor) {
        ChatUser user = connected();
        ChatRoom chatRoom = ChatRoom.findById(roomId);
        if (!chatRoom.isModerator(user)) {
            error("Not a moderator of '" + chatRoom.getName() + "'.");
        } else {
            chatRoom.setSidebarBackgroundColor(sidebarBackgroundColor);
            chatRoom.setSidebarTextColor(sidebarTextColor);
            chatRoom.setSidebarRoomSelectedColor(sidebarRoomSelectedColor);
            chatRoom.setSidebarRoomHoverColor(sidebarRoomHoverColor);
            chatRoom.setSidebarRoomTextColor(sidebarRoomTextColor);
            chatRoom.setSidebarUnreadColor(sidebarUnreadColor);
            chatRoom.setSidebarUnreadTextColor(sidebarUnreadTextColor);
            chatRoom.setSigninButtonColor(signinButtonColor);
            chatRoom.setSigninButtonTextColor(signinButtonTextColor);
            chatRoom.save();
            TopAlert alert = new TopAlert(TopAlert.Type.SUCCESS, "Room updated.", "Room information saved.");
            alert.toFlash(flash);
            WebSocket.room(chatRoom.getName());
        }
    }

}
