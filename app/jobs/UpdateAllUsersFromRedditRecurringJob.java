package jobs;

import models.ChatUser;
import play.Logger;
import play.jobs.Every;
import play.jobs.Job;

import java.util.List;

/**
 * Created by matt on 4/27/16.
 */
@Every("1h")
public class UpdateAllUsersFromRedditRecurringJob extends Job {

    @Override
    public void doJob() throws Exception {
        Logger.info("Queueing user update jobs...");
        List<ChatUser> allUsers = ChatUser.findAll();
        int count = 0;
        for (ChatUser user : allUsers) {
            if (user.isNotBanned()) {
                new UpdateUserFromRedditJob(user.getId()).now();
                count++;
            }
        }
        Logger.info("Queued " + count + " jobs.");
    }
}
