package jobs;

import com.larvalabs.redditchat.util.RedditUtil;
import models.ChatRoom;
import models.ChatUser;
import models.Message;
import play.Logger;
import play.jobs.Job;
import play.mvc.Router;

import java.util.HashMap;
import java.util.Set;

public class NotifyMentionedUsersJob extends Job {

    Long messageId;

    public NotifyMentionedUsersJob(Long messageId) {
        this.messageId = messageId;
    }

    public void doJob() throws Exception {
        Logger.info("Processing message ID " + messageId + " for mentioned users.");
        Message message = Message.findById(messageId);
        ChatRoom chatRoom = message.getRoom();

        HashMap<String, Object> params = new HashMap<String, Object>();
        params.put("roomName", chatRoom.getName());
        Router.ActionDefinition reverse = Router.reverse("WebSocket.room", params);
        reverse.absolute();

        Set<ChatUser> mentionedUsers = message.updateMentionedUsers();
        if (mentionedUsers != null && mentionedUsers.size() > 0) {
            for (ChatUser mentionedUser : mentionedUsers) {
                Logger.info("User mentioned: " + mentionedUser.getUsername() + " in message id " + message.getId());
                if (mentionedUser.isNotificationEnabledForMention()) {

                    String subject = "You were mentioneed in "+chatRoom.getName();
                    String content = message.getUser().getUsername() + " mentioned you in " + chatRoom.getName() + ":\n\n "
                            + message.getMessageText() + "\n\n"
                            + "Go to " + reverse.url + " to response.";
                    RedditUtil.sendPrivateMessageFromBot(mentionedUser.getUsername(), subject, content);
                }
            }
        }
    }

}
