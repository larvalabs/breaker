package jobs;

import com.larvalabs.redditchat.Constants;
import com.larvalabs.redditchat.util.RedisUtil;
import models.ChatUser;
import models.ChatUserRoomJoin;
import play.Logger;
import play.jobs.Job;
import play.jobs.On;

import java.util.List;

/**
 * Created by matt on 7/1/16.
 */
@On("0 0 2 * * ?")
public class DailyCleanupJob extends Job {

    @Override
    public void doJob() throws Exception {
        Logger.info("Daily cleanup job starting.");
        ChatUser guestUser = ChatUser.findByUsername(Constants.USERNAME_GUEST);
        List<ChatUserRoomJoin> guestJoins = ChatUserRoomJoin.findByUser(guestUser);
        for (ChatUserRoomJoin guestJoin : guestJoins) {
            if (!guestJoin.getRoom().isDefaultRoom()) {
                Logger.info("Removing guest user from room " + guestJoin.getRoom().getName());
                guestJoin.delete();
            }
        }

        Logger.info("Cleaning all presence sets.");
        RedisUtil.cleanAllPresenceSets();

        Logger.info("Daily cleanup job done.");
    }
}
