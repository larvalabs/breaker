package jobs;

import com.larvalabs.redditchat.util.RedditUtil;
import com.larvalabs.redditchat.util.Stats;
import models.*;
import net.dean.jraw.ApiException;
import net.dean.jraw.http.NetworkException;
import play.Logger;
import play.Play;
import play.jobs.Job;
import play.mvc.Router;

import java.util.ArrayList;
import java.util.List;

public class NotifyNewMessagesJob extends Job<Boolean> {

    Long userId;

    public NotifyNewMessagesJob(Long userId) {
        this.userId = userId;
    }

    @Override
    public Boolean doJobWithResult() throws Exception {
        Logger.info("Processing user " + userId + " for new message notifications.");
        ChatUser user = ChatUser.findById(userId);
        if (!user.isNotificationEnabledForEverything() || OptOutUser.didOptOut(user.getUsername())) {
            Logger.info("User " + user.getUsername() + " does not have all notifications enabled, not sending new messages notification.");
            return false;
        }

        List<ChatUserRoomJoin> chatRoomJoins = user.getChatRoomJoins();
        NewMessageInfo newMessageInfo = new NewMessageInfo(chatRoomJoins).invoke();
        ArrayList<ChatRoom> roomsWithNewMessages = newMessageInfo.getRoomsWithNewMessages();
        long totalNewMessages = newMessageInfo.getTotalNewMessages();

        if (roomsWithNewMessages.size() > 0) {
            NewMessagesNotification newMessagesNotification = new NewMessagesNotification(roomsWithNewMessages, totalNewMessages).invoke();
            String subject = newMessagesNotification.getSubject();
            String content = newMessagesNotification.getMessageBody();
            try {
                RedditUtil.sendPrivateMessageFromBot(user.getUsername(), subject, content);
                Stats.count(Stats.StatKey.REDDIT_NEWMSGNOTIFICATION_SUCCESS, 1);
                return true;
            } catch (NetworkException | ApiException e) {
                Stats.count(Stats.StatKey.REDDIT_NEWMSGNOTIFICATION_FAILED, 1);
                e.printStackTrace();
                return false;
            }
        }

        return false;
    }

    public static class NewMessageInfo {
        private List<ChatUserRoomJoin> chatRoomJoins;
        private ArrayList<ChatRoom> roomsWithNewMessages;
        private long totalNewMessages;

        public NewMessageInfo(List<ChatUserRoomJoin> chatRoomJoins) {
            this.chatRoomJoins = chatRoomJoins;
        }

        public ArrayList<ChatRoom> getRoomsWithNewMessages() {
            return roomsWithNewMessages;
        }

        public long getTotalNewMessages() {
            return totalNewMessages;
        }

        public NewMessageInfo invoke() {
            roomsWithNewMessages = new ArrayList<>();
            totalNewMessages = 0;
            for (ChatUserRoomJoin chatRoomJoin : chatRoomJoins) {
                if (!chatRoomJoin.getRoom().isDeleted()) {
                    long numNewMessages = chatRoomJoin.getNumNewMessagesIgnoringBots();
                    if (numNewMessages > 0) {
                        roomsWithNewMessages.add(chatRoomJoin.getRoom());
                        totalNewMessages += numNewMessages;
                    }
                }
            }
            return this;
        }
    }

    public static class NewMessagesNotification {
        private ArrayList<ChatRoom> roomsWithNewMessages;
        private long totalNewMessages;
        private String subject;
        private String messageBody;

        public NewMessagesNotification(ArrayList<ChatRoom> roomsWithNewMessages, long totalNewMessages) {
            this.roomsWithNewMessages = roomsWithNewMessages;
            this.totalNewMessages = totalNewMessages;
        }

        public String getSubject() {
            return subject;
        }

        public String getMessageBody() {
            return messageBody;
        }

        public NewMessagesNotification invoke() {
            ChatRoom chatRoom = roomsWithNewMessages.get(0);

            String baseUrl = Play.configuration.getProperty("application.baseUrl");
            String url = baseUrl + "/r/" + chatRoom.getName();

            String prefsUrl = baseUrl + Router.reverse("UserManage.prefs").url;

            int otherRoomsCount = roomsWithNewMessages.size() - 1;
            subject = "You have " + totalNewMessages + " new messages in Breaker";
            messageBody = "You have " + totalNewMessages + " new messages in [" + chatRoom.getName() + " chat](" + url + ")";
            if (otherRoomsCount > 0) {
                messageBody += " and " + otherRoomsCount + " other rooms. ";
            } else {
                messageBody += ".";
            }
            messageBody += "\n\n\n"
                    + "[Click to view new messages](" + url + ") | "
                    + "[Change your notifications preferences](" + prefsUrl + ")";
            return this;
        }
    }
}
