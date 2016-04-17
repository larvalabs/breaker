package jobs;

import com.larvalabs.redditchat.util.Stats;
import models.ChatRoom;
import play.Invoker;
import play.Logger;
import play.jobs.Every;
import play.jobs.Job;
import play.jobs.JobsPlugin;

import java.util.HashSet;
import java.util.List;

/**
 * Created by matt on 4/15/16.
 */
@Every("30s")
public class RecurringStatsJob extends Job {

    @Override
    public void doJob() throws Exception {
        long start = System.currentTimeMillis();
        Stats.sample(Stats.StatKey.JOB_ACTIVE, JobsPlugin.executor.getActiveCount() - 1); // -1 so we don't count this job
        Stats.sample(Stats.StatKey.JOB_QUEUED, JobsPlugin.executor.getQueue().size());

        Stats.sample(Stats.StatKey.REQUESTS_ACTIVE, Invoker.executor.getActiveCount());
        Stats.sample(Stats.StatKey.REQUESTS_QUEUED, Invoker.executor.getQueue().size());

        Stats.sample(Stats.StatKey.USERS_CONNECTED, ChatRoom.getAllOnlineUsersForAllRooms().size());

        Logger.info("Stats job runtime: " + (System.currentTimeMillis() - start));
    }
}
