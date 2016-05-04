package jobs;

import com.amazonaws.util.json.JSONException;
import com.amazonaws.util.json.JSONObject;
import com.larvalabs.redditchat.dataobj.BreakerCache;
import models.ChatRoom;
import models.ChatUser;
import models.ChatUserRoomJoin;
import play.Logger;
import play.db.jpa.JPABase;
import play.jobs.Job;
import play.jobs.OnApplicationStart;
import reddit.BreakerRedditClient;
import reddit.RedditRequestError;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by matt on 1/5/16.
 */
//@OnApplicationStart
public class UpdateUserFromRedditJob extends Job {

    private long userId;
    private boolean clearCacheWhenDone = false;

    public UpdateUserFromRedditJob(long userId) {
        this.userId = userId;
    }

    public UpdateUserFromRedditJob(long userId, boolean clearCacheWhenDone) {
        this.userId = userId;
        this.clearCacheWhenDone = clearCacheWhenDone;
    }

    @Override
    public void doJob() throws Exception {
        // get user from reddit
        // save flair
        // Mock user
        ChatUser chatUser = ChatUser.findById(userId);
//        ChatUser chatUser = new ChatUser("1");
//        chatUser.username = "mathent";
//        chatUser.accessToken = "9567379-IQp3H3AeY0XM2ci-E5NzotmGMHU";
//        chatUser.refreshToken = "9567379-7ARi2_GnU49mCpJUIzbGBmrCKpk";

        // Refresh access token for user
        BreakerRedditClient breakerRedditClient = new BreakerRedditClient();
        List<ChatUserRoomJoin> chatRoomJoins = chatUser.getChatRoomJoins();
        for (ChatUserRoomJoin chatUserRoomJoin : chatRoomJoins) {
            try {
                JSONObject jsonFlairObj = breakerRedditClient.getRedditUserFlairForSubreddit(chatUser, chatUserRoomJoin.getRoom().getName());
                JSONObject currentFlair = jsonFlairObj.getJSONObject("current");
                chatUserRoomJoin.setFlairText(currentFlair.getString("flair_text"));
                chatUserRoomJoin.setFlairCss(currentFlair.getString("flair_css_class"));
                chatUserRoomJoin.setFlairPosition(currentFlair.getString("flair_position"));
                chatUserRoomJoin.save();
                Logger.info("User " + chatUser.getUsername() + " updated flair for room " + chatUserRoomJoin.room.getName() + " from reddit.");
            } catch (RedditRequestError redditRequestError) {
                Logger.warn("Reddit request error for user " + chatUser.getUsername() + " for room " + chatUserRoomJoin.room.getName());
            } catch (JSONException e) {
                Logger.error(e, "Error parsing json result from reddit request.");
            } catch (Exception e) {
                Logger.error(e, "Problem getting flair for room: " + chatUserRoomJoin.getRoom().getName());
            }
        }

        // Make user a moderator of subs
        ArrayList<String> subNamesModerated = breakerRedditClient.getSubNamesModerated(chatUser);
        Logger.info("User " + chatUser.getUsername() + " is a moderator of " + subNamesModerated.size() + " subs.");
        for (String subName : subNamesModerated) {
            ChatRoom chatRoom = ChatRoom.findByName(subName);
            if (chatRoom != null) {
                chatUser.moderateRoom(chatRoom);
                chatUser.joinChatRoom(chatRoom);
                Logger.info("User made moderator of room " + chatRoom.getName());
            } else {
                Logger.info("User is moderator of room " + subName + " but it doesn't exist yet, not creating.");
            }
        }

        if (clearCacheWhenDone) {
            BreakerCache.clearUsersCacheAll();
        }
    }
}
