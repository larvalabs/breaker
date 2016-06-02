package jobs;

import models.ChatUser;
import play.Logger;
import play.jobs.Every;
import play.jobs.Job;
import play.jobs.On;

import java.util.List;

/**
 * Created by matt on 4/27/16.
 */
//@Every("24h")
//@On("0 0 8 * * ?")
public class NotifyNewMessagesRecurringJob extends Job {

    @Override
    public void doJob() throws Exception {
        Logger.info("Queueing user new message notification jobs...");
        List<ChatUser> allUsers = ChatUser.findAll();
        int count = 0;
        for (int i = 0; i < allUsers.size(); i++) {
            ChatUser user = allUsers.get(i);
            if (user.isNotBanned() && !user.isBot()) {
                new NotifyNewMessagesJob(user.getId()).now();
                count++;
            }
        }
        Logger.info("Queued " + count + " jobs.");
    }
}
