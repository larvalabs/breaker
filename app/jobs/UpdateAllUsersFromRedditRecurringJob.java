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
        for (int i = 0; i < allUsers.size(); i++) {
            ChatUser user = allUsers.get(i);
            boolean clearCache = false;
            if (i == allUsers.size() - 1) {
                // todo this isn't ideal because the jobs get run out of order, will think about something better
                clearCache = true;
            }
            new UpdateUserFromRedditJob(user.getId(), clearCache).now();
            count++;
        }
        Logger.info("Queued " + count + " jobs.");
    }
}
