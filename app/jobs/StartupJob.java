package jobs;

import com.larvalabs.redditchat.realtime.ChatRoomStream;
import models.ChatRoom;
import play.Logger;
import play.db.jpa.JPABase;
import play.jobs.Job;
import play.jobs.OnApplicationStart;
import play.modules.redis.Redis;
import redis.clients.jedis.JedisPubSub;

import java.util.List;

/**
 * Created by matt on 12/28/15.
 */
@OnApplicationStart
public class StartupJob extends Job {


    @Override
    public void doJob() throws Exception {
        // Set http agent so reddit doesn't throttle us
//        System.setProperty("http.agent", "web:breakerapp:v0.1");

        Logger.info("Loading cache of last messages on application startup.");
        List<ChatRoom> rooms = ChatRoom.findAll();
        for (ChatRoom room : rooms) {
            new LoadMessageCacheJob(room.getId()).now();
        }
    }
}
