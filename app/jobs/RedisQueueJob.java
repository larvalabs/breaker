package jobs;

import com.larvalabs.redditchat.realtime.ChatRoomStream;
import play.Logger;
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
//            Logger.debug("Received message on redis channel " + channel + ": " + message);
            ChatRoomStream.Event event = ChatRoomStream.Event.fromJson(message);
            if (event instanceof ChatRoomStream.Message) {
                ChatRoomStream.getMessageStream(event.room.name).publishEventInternal(event);
            } else {
                ChatRoomStream.getEventStream(event.room.name).publishEventInternal(event);
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
            Logger.info("Redis queue UNsubscribed.");
        }

        @Override
        public void onPUnsubscribe(String s, int i) {

        }

        @Override
        public void onPSubscribe(String s, int i) {

        }
    }

    public static void publish(ChatRoomStream.Event event) {
        Logger.debug("Sending event to redis");
        Redis.publish(CHANNEL, event.toJson());
    }
}
