package jobs;

import com.larvalabs.redditchat.dataobj.BreakerCache;
import models.ChatRoom;
import play.jobs.Job;
import play.jobs.OnApplicationStart;

/**
 * Created by matt on 12/28/15.
 */
public class LoadMessageCacheJob extends Job {

    public long roomId;

    public LoadMessageCacheJob(long roomId) {
        this.roomId = roomId;
    }

    @Override
    public void doJob() throws Exception {
        BreakerCache.preloadCacheForRoom((ChatRoom) ChatRoom.findById(roomId));
    }
}
