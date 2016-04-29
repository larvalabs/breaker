package jobs;

import com.larvalabs.redditchat.util.RedditUtil;
import controllers.Application;
import models.ChatRoom;
import models.ChatUser;
import models.Message;
import play.Logger;
import play.Play;
import play.jobs.Job;
import play.mvc.Router;

import java.util.HashMap;
import java.util.Set;
import java.util.TreeSet;

public class NotifyMentionedUsersJob extends Job {

    Long messageId;

    public NotifyMentionedUsersJob(Long messageId) {
        this.messageId = messageId;
    }

    public void doJob() throws Exception {
        Logger.info("Processing message ID " + messageId + " for mentioned users.");
        Message message = Message.findById(messageId);
        ChatRoom chatRoom = message.getRoom();

/*
        HashMap<String, Object> params = new HashMap<String, Object>();
        params.put("roomName", chatRoom.getName());
        Router.ActionDefinition reverse = Router.reverse("WebSocket.room", params);
        reverse.absolute();
*/
        String baseUrl = Play.configuration.getProperty("application.baseUrl");
        String url = baseUrl + "/c/" + chatRoom.getName();

        TreeSet<String> allOnlineUsersForAllRooms = ChatRoom.getAllOnlineUsersForAllRooms();

        Set<ChatUser> mentionedUsers = message.updateMentionedUsers();
        if (mentionedUsers != null && mentionedUsers.size() > 0) {
            for (ChatUser mentionedUser : mentionedUsers) {
                Logger.info("User mentioned: " + mentionedUser.getUsername() + " in message id " + message.getId());
                if (mentionedUser.isNotificationEnabledForMention() && !allOnlineUsersForAllRooms.contains(mentionedUser.getUsername())) {

                    String subject = "You were mentioned in "+chatRoom.getName() + " chat";
                    String content = message.getUser().getUsername() + " mentioned you in " + chatRoom.getName() + " chat:\n\n "
                            + "\"" + message.getMessageText() + "\"\n\n"
                            + "Go to " + url + " to respond.\n\n"
                            + "\n"
                            + "Notification preferences at https://www.breakerapp.com/usermanage/prefs";
                    RedditUtil.sendPrivateMessageFromBot(mentionedUser.getUsername(), subject, content);
                } else {
                    Logger.info("Not notifying mentioned user "+mentionedUser.getUsername()+" because of notification preference or because they are online.");
                }
            }
        }
    }

}
