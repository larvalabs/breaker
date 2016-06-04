package com.larvalabs.redditchat.util;

import play.Logger;

/**
 * Created by matt on 4/15/16.
 */
public class Stats {

    public enum StatKey {
        JOB_ACTIVE("jobs.active"),
        JOB_QUEUED("jobs.queued"),
        MESSAGE("messages"),
        USERS_CONNECTED("users.connected"),
        REQUESTS_ACTIVE("requests.active"),
        REQUESTS_QUEUED("requests.queued"),
        WEBSOCKET_CONNECT("websocket.connect"), // Using this until I can find a place to get all connected sockets
        WEBSOCKET_JOIN_TIME("websocket.jointime"),
        INITIALPAGE_TIME("initialpage"),
        USER_STREAMS_OPEN("userstreams"),
        REDIS_MESSAGES("redismessage"),
        REDDIT_PM_SUCCESS("redditpm.success"),
        REDDIT_PM_FAILED("redditpm.failed"),
        REDDIT_NEWMSGNOTIFICATION_SUCCESS("redditnewmsgnotification.success"),
        REDDIT_NEWMSGNOTIFICATION_FAILED("redditnewmsgnotification.failed"),
        USER_CACHE_SIZE("usercache.size"),
        ROOM_CACHE_SIZE("roomcache.size"),
        REDIS_TIMING_USERPRESENT("redis.timing.userpresent"),
        REDIS_TIMING_PIPELINEUSERPRESENT("redis.timing.pipelineuserpresent"),
        LOAD_FULLSTATE_TIME("fullstate.timing.load")
        ;

        private String key;

        StatKey(String key) {
            this.key = key;
        }

        public String getKey() {
            return key;
        }
    }

    public static String KEY_JOB_ACTIVE = "jobs.active";
    public static String KEY_JOB_QUEUED = "jobs.queued";
    public static String KEY_MESSAGE = "messages";
    public static String KEY_USERS_CONNECTED = "users.connected";

    public static void count(StatKey key, long value) {
        Logger.info("count#" + key.getKey() + "=" + value);
    }

    public static void sample(StatKey key, float value) {
        Logger.info("sample#" + key.getKey() + "=" + value);
    }

    public static void sample(StatKey key, long value) {
        Logger.info("sample#" + key.getKey() + "=" + value);
    }

    public static void measure(StatKey key, long timeMs) {
        Logger.info("measure#" + key.getKey() + "=" + timeMs + "ms");
    }
}
