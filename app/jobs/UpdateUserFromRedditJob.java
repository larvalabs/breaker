package jobs;

import com.amazonaws.util.json.JSONException;
import com.amazonaws.util.json.JSONObject;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.larvalabs.redditchat.dataobj.BreakerCache;
import com.larvalabs.redditchat.realtime.ChatRoomStream;
import models.ChatRoom;
import models.ChatUser;
import models.ChatUserRoomJoin;
import play.Logger;
import play.jobs.Job;
import reddit.BreakerRedditClient;
import reddit.RedditRequestError;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
        boolean userOnline = chatUser.isUserOnlineInAnyRoom();

//        ChatUser chatUser = new ChatUser("1");
//        chatUser.username = "mathent";
//        chatUser.accessToken = "9567379-IQp3H3AeY0XM2ci-E5NzotmGMHU";
//        chatUser.refreshToken = "9567379-7ARi2_GnU49mCpJUIzbGBmrCKpk";

        // Refresh access token for user
        BreakerRedditClient breakerRedditClient = new BreakerRedditClient();

        JSONObject redditUserDetails = breakerRedditClient.getRedditUserDetails(chatUser);
        // todo This json object type conversion seems a bit unfortunate
        JsonParser jsonParser = new JsonParser();
        chatUser.updateUserFromRedditJson((JsonObject) jsonParser.parse(redditUserDetails.toString()));
        chatUser.save();

        List<ChatUserRoomJoin> chatRoomJoins = chatUser.getChatRoomJoins();
        for (ChatUserRoomJoin chatUserRoomJoin : chatRoomJoins) {
            ChatRoom room = chatUserRoomJoin.getRoom();
            try {
                JSONObject jsonFlairObj = breakerRedditClient.getRedditUserFlairForSubreddit(chatUser, room.getName());
                JSONObject currentFlair = jsonFlairObj.getJSONObject("current");
                String newFlairText = currentFlair.getString("flair_text");
                String newFlairCss = currentFlair.getString("flair_css_class");
                String newFlairPosition = currentFlair.getString("flair_position");
                boolean changed = chatUserRoomJoin.updateFlairIfDifferent(newFlairText, newFlairCss, newFlairPosition);
                if (changed) {
                    Logger.info("User " + chatUser.getUsername() + " has new flair for room " + chatUserRoomJoin.room.getName() + " from reddit, updating user object.");
                    chatUserRoomJoin.save();
                    ChatRoomStream eventStream = ChatRoomStream.getEventStream(room.getName());
                    eventStream.sendUserUpdate(room, chatUser, userOnline);
                }
            } catch (RedditRequestError redditRequestError) {
                Logger.warn("Reddit request error for user " + chatUser.getUsername() + " for room " + chatUserRoomJoin.room.getName());
            } catch (JSONException e) {
                Logger.error(e, "Error parsing json result from reddit request.");
            } catch (Exception e) {
                Logger.error(e, "Problem getting flair for room: " + room.getName());
            }
        }

        // Make user a moderator of subs
        ArrayList<String> subNamesModerated = breakerRedditClient.getSubNamesModerated(chatUser);
        Set<ChatRoom> moderatedRooms = new HashSet<>(chatUser.getModeratedRooms());

        Logger.info("User " + chatUser.getUsername() + " is a moderator of " + subNamesModerated.size() + " subs.");
        for (String subName : subNamesModerated) {
            ChatRoom room = getRoomForName(subName, moderatedRooms);
            if (room != null) {
                Logger.info("User is already mod of room " + room.getName());
                moderatedRooms.remove(room);    // This does not update database, just for later update step
            } else {
                ChatRoom newModRoom = ChatRoom.findByName(subName);
                if (newModRoom != null) {
                    chatUser.moderateRoom(newModRoom);
                    chatUser.joinChatRoom(newModRoom);
                    Logger.info("User made moderator of room " + newModRoom.getName());
                    ChatRoomStream eventStream = ChatRoomStream.getEventStream(newModRoom.getName());
                    eventStream.sendUserUpdate(newModRoom, chatUser, userOnline);
                    eventStream.sendRoomUpdate(newModRoom);
                } else {
                    Logger.info("User is moderator of room " + subName + " but it doesn't exist yet in our database, not creating.");
                }
            }
        }

        // Process any remaining mod joins in our DB, these are rooms user is not longer a mod of
        for (ChatRoom moderatedRoom : moderatedRooms) {
            chatUser.stopModerating(moderatedRoom);
            ChatRoomStream eventStream = ChatRoomStream.getEventStream(moderatedRoom.getName());
            eventStream.sendRoomUpdate(moderatedRoom);
            Logger.info("User is no longer mod of room " + moderatedRoom.getName() + " because the reddit response didn't have them listed as mod.");
        }

        if (clearCacheWhenDone) {
            BreakerCache.clearUsersCacheAll();
        }
    }

    private ChatRoom getRoomForName(String name, Set<ChatRoom> rooms) {
        for (ChatRoom room : rooms) {
            if (room.getName().toLowerCase().equals(name.toLowerCase())) {
                return room;
            }
        }
        return null;
    }
}
