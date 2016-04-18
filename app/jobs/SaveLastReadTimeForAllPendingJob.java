package jobs;

import models.ChatRoom;
import play.Logger;
import play.jobs.Every;
import play.jobs.Job;
import play.modules.redis.Redis;

import java.util.List;
import java.util.Set;

/**
 * Created by matt on 1/5/16.
 */
@Every("1min")
public class SaveLastReadTimeForAllPendingJob extends Job {

    public static final String REDISKEY_PENDINGUSERNAMES = "savelastreadpending";

    public static void addPendingUsername(String username, String roomName) {
        try {
            Redis.sadd(REDISKEY_PENDINGUSERNAMES, combineUsernameAndRoom(username, roomName));
        } catch (Exception e) {
            Logger.error(e, "Error setting pending username.");
        }
    }

    private static String[] splitUsernameAndRoom(String combined) {
        return combined.split(":");
    }

    private static String combineUsernameAndRoom(String username, String room) {
        return username + ":" + room;
    }

    @Override
    public void doJob() throws Exception {
        Logger.info("Starting periodic save of last read values for all users job.");
        Set<String> combinedPendingToSave = Redis.smembers(REDISKEY_PENDINGUSERNAMES);
        Logger.info("Queuing " + combinedPendingToSave.size() + " user / room combos to save in sub jobs.");
        Redis.del(new String[]{REDISKEY_PENDINGUSERNAMES});
        for (String combined : combinedPendingToSave) {
            String[] parts = splitUsernameAndRoom(combined);
            new SaveLastReadForUserJob(parts[0], parts[1]).now();
        }
    }
}
