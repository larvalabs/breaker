package jobs;

import com.larvalabs.redditchat.dataobj.BreakerCache;
import com.larvalabs.redditchat.realtime.ChatRoomStream;
import com.larvalabs.redditchat.util.Stats;
import play.Logger;
import play.Play;
import play.jobs.Job;
import play.jobs.OnApplicationStart;
import play.modules.redis.Redis;
import play.modules.redis.RedisConnectionManager;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;

/**
 * Created by matt on 12/28/15.
 */
@OnApplicationStart(async = true)
public class RedisQueueJob extends Job {

    private static final String CHANNEL = "chat_default";

    @Override
    public void doJob() throws Exception {
//        Jedis j = RedisConnectionManager.getRawConnection();
        Redis.subscribe(new RedisListener(), new String[]{CHANNEL});
    }

    private static class RedisListener extends JedisPubSub {
        @Override
        public void onMessage(String channel, String message) {
//            Logger.info("Received message on redis channel " + channel + ": " + message);
            ChatRoomStream.Event event = ChatRoomStream.Event.fromJson(message);
            if (!event.fromServerID.equals(ChatRoomStream.SERVER_ID)) {
                ChatRoomStream.getEventStream(event.room.name).publishEventInternal(event);
            } else {
//                Logger.info("Not processing message from redis queue, it comes from us: " + ChatRoomStream.SERVER_ID);
            }
        }

        @Override
        public void onPMessage(String s, String s1, String s2) {

        }

        @Override
        public void onSubscribe(String s, int i) {
            Logger.info("Redis queue subscribed.");
        }

        @Override
        public void onUnsubscribe(String s, int i) {
            Logger.info("Redis queue unsubscribed.");
        }

        @Override
        public void onPUnsubscribe(String s, int i) {
            Logger.info("Redis queue p unsubscribed.");
        }

        @Override
        public void onPSubscribe(String s, int i) {
            Logger.info("Redis queue p subscribed.");
        }
    }

    public static void publish(ChatRoomStream.Event event) {
//        Logger.info("Sending event to redis");
        if (Play.mode.isProd()) {
//            Stats.count(Stats.StatKey.REDIS_MESSAGES, 1);
        }
        Redis.publish(CHANNEL, event.toJson());
    }
}
