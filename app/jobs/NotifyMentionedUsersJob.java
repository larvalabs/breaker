package jobs;

import com.larvalabs.redditchat.dataobj.JsonChatRoom;
import com.larvalabs.redditchat.realtime.ChatRoomStream;
import com.larvalabs.redditchat.util.RedditUtil;
import com.larvalabs.redditchat.util.Stats;
import controllers.Application;
import models.ChatRoom;
import models.ChatUser;
import models.Message;
import models.OptOutUser;
import net.dean.jraw.ApiException;
import net.dean.jraw.http.NetworkException;
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
                ChatRoomStream eventStream = ChatRoomStream.getEventStream(message.getRoom().getName());
                if (mentionedUser.isNotificationEnabledForMention() && !allOnlineUsersForAllRooms.contains(mentionedUser.getUsername())
                        && !OptOutUser.didOptOut(mentionedUser.getUsername())) {

                    String subject = "You were mentioned in "+chatRoom.getName() + " chat";
                    String content = "/u/" + message.getUser().getUsername() + " mentioned you in " + chatRoom.getName() + " chat:\n\n "
                            + "\"" + message.getMessageText() + "\"\n\n"
                            + "To respond: " + url + "\n\n"
                            + "To opt out of any future messages: https://www.breakerapp.com/optout/" + message.getUuid() + "/" + mentionedUser.getUsername();
                    try {
                        RedditUtil.sendPrivateMessageFromBot(mentionedUser.getUsername(), subject, content);
                        eventStream.sayFromServer(JsonChatRoom.from(chatRoom, chatRoom.getModeratorUsernames()), message.getUser().getUsername(),
                                "User " + mentionedUser.getUsername() + " is offline, so we notified them via PM on Reddit.");
                        Stats.count(Stats.StatKey.REDDIT_PM_SUCCESS, 1);
                    } catch (NetworkException | ApiException e) {
                        eventStream.sayFromServer(JsonChatRoom.from(chatRoom, chatRoom.getModeratorUsernames()), message.getUser().getUsername(),
                                "Failed to notify " + mentionedUser.getUsername() + " via Reddit PM, please notify an admin.");
                        Stats.count(Stats.StatKey.REDDIT_PM_FAILED, 1);
                        e.printStackTrace();
                    }
                } else {
                    Logger.info("Not notifying mentioned user "+mentionedUser.getUsername()+" because of notification preference or because they are online.");
                    eventStream.sayFromServer(JsonChatRoom.from(chatRoom, chatRoom.getModeratorUsernames()), message.getUser().getUsername(),
                            "Not notifying " + mentionedUser.getUsername() + " via Reddit PM, they have requested to not receive PMs.");
                }
            }
        }
    }

}
