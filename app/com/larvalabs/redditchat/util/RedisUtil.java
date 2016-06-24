package com.larvalabs.redditchat.util;

import com.larvalabs.redditchat.Constants;
import com.larvalabs.redditchat.dataobj.JsonActiveChatRoom;
import com.sun.istack.internal.Nullable;
import models.ChatUser;
import models.ChatUserRoomJoin;
import play.Logger;
import play.Play;
import play.modules.redis.Redis;
import redis.clients.jedis.Pipeline;

import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;

/**
 * Utility methods for things managed by Redis. Mainly user room presence right now.
 * There are a number of convenience methods in ChatRoom for working with user presence for a specific room.
 */
public class RedisUtil {

    public static final String REDISKEY_PRESENCE_GLOBAL = "presence__global";
    public static final String REDISKEY_ACTIVE_ROOMS = "active__rooms";
    public static final double CHANCE_CLEAN_REDIS_PRESENCE = 0.01;

    private static Random random = new Random();

    public static void clearAllKeys() {
        Redis.flushDB();
    }

    // note: could consider doing this as a separate set of just usernames
    public static TreeSet<String> getUsernamesPresent(String roomName) {
        try {
            int time = (int) (System.currentTimeMillis() / 1000);
            Set<String> usersPresent = Redis.zrangeByScore(getRedisPresenceKeyForRoom(roomName), time - Constants.PRESENCE_TIMEOUT_SEC, time);
            TreeSet<String> usernamesPresent = new TreeSet<String>();
            for (String usernameAndConnStr : usersPresent) {
                usernamesPresent.add(splitUsernameAndConnection(usernameAndConnStr)[0]);
            }

            return usernamesPresent;
        } catch (Exception e) {
            Logger.error(e, "Error contacting redis.");
            return new TreeSet<String>();
        }
    }

    public static boolean isUserPresent(String roomName, String username) {
        return getUsernamesPresent(roomName).contains(username);
    }

    public static void userPresent(String roomName, String username, String connectionId) {
        try {
            long startTime = System.currentTimeMillis();
            int time = (int) (System.currentTimeMillis() / 1000);
            Redis.zadd(getRedisPresenceKeyForRoom(roomName), time, getUsernameAndConnectionString(username, connectionId));
            userPresentGlobal(username, connectionId);
            if (random.nextFloat() < CHANCE_CLEAN_REDIS_PRESENCE) {
                // this is just housekeeping to keep the sets from getting too big
                cleanPresenceSet(roomName);

                // Just occasionally measure timing for this to avoid overloading logs
//                Stats.measure(Stats.StatKey.REDIS_TIMING_USERPRESENT, (System.currentTimeMillis() - startTime));
            }
        } catch (Exception e) {
            Logger.error(e, "Error contacting redis.");
        }
    }

    /**
     * Set user present for all rooms present in the joins list. Executed with a redis pipeline so you save a roundtrip
     * per room.
     * @param username
     * @param connectionId
     * @param joins
     */
    public static void markUserPresentForAllTheirRooms(String username, String connectionId, List<ChatUserRoomJoin> joins) {
        long startTime = System.currentTimeMillis();
        int time = (int) (System.currentTimeMillis() / 1000);
        Pipeline pipeline = Redis.pipelined();
        for (ChatUserRoomJoin roomJoin : joins) {
            pipeline.zadd(getRedisPresenceKeyForRoom(roomJoin.getRoom().getName()), time, getUsernameAndConnectionString(username, connectionId));
        }
        pipeline.zadd(REDISKEY_PRESENCE_GLOBAL, time, getUsernameAndConnectionString(username, connectionId));
        pipeline.sync();
        Stats.measure(Stats.StatKey.REDIS_TIMING_PIPELINEUSERPRESENT, (System.currentTimeMillis() - startTime));
    }

    private static String getUsernameAndConnectionString(String username, String connectionId) {
        return username + ":" + connectionId;
    }

    /**
     * Add to list of all online users across rooms
     * @param username
     */
    public static void userPresentGlobal(String username, String connectionId) {
        try {
            int time = (int) (System.currentTimeMillis() / 1000);
            Redis.zadd(REDISKEY_PRESENCE_GLOBAL, time, getUsernameAndConnectionString(username, connectionId));
            if (random.nextFloat() < CHANCE_CLEAN_REDIS_PRESENCE) {
                // this is just housekeeping to keep the sets from getting too big
                cleanPresenceSetGlobal();
            }
        } catch (Exception e) {
            Logger.error(e, "Error contacting redis.");
        }
    }

    public static void userNotPresentGlobal(String username, String connectionId) {
        try {
            Redis.zrem(REDISKEY_PRESENCE_GLOBAL, getUsernameAndConnectionString(username, connectionId));
        } catch (Exception e) {
            Logger.error(e, "Error contacting redis.");
        }
    }

    /**
     * Housekeeping to keep list sizes under control
     */
    private static void cleanPresenceSetGlobal() {
        try {
            int time = (int) (System.currentTimeMillis() / 1000);
            Long removed = Redis.zremrangeByScore(REDISKEY_PRESENCE_GLOBAL, 0, time - Constants.PRESENCE_TIMEOUT_SEC * 2);
//            Logger.debug("Clean of presence set for " + name + " removed " + removed + " elements.");
        } catch (Exception e) {
            Logger.error(e, "Error contacting redis.");
        }
    }

    /**
     * This might be better in another class
     * @return List of all online usernames, across all rooms.
     */
    public static TreeSet<String> getAllOnlineUsersForAllRooms() {
        try {
            int time = (int) (System.currentTimeMillis() / 1000);
            Set<String> usersPresent = Redis.zrangeByScore(REDISKEY_PRESENCE_GLOBAL, time - Constants.PRESENCE_TIMEOUT_SEC, time);
            TreeSet<String> usernamesPresent = new TreeSet<String>();
            for (String usernameAndConnection : usersPresent) {
                String[] chunks = splitUsernameAndConnection(usernameAndConnection);
                if (chunks.length == 1) {
                    usernamesPresent.add(usernameAndConnection);
                } else {
                    usernamesPresent.add(chunks[0]);
                }
            }

            return usernamesPresent;
        } catch (Exception e) {
            Logger.error(e, "Error contacting redis.");
            return new TreeSet<String>();
        }
    }

    public static boolean isUserOnlineInAnyRoom(String username) {
        return getAllOnlineUsersForAllRooms().contains(username);
    }

    private static String getRedisPresenceKeyForRoom(String roomName) {
        return "presence_" + roomName;
    }

    /**
     * @return [0] = username, [1] = connectionId
     */
    private static String[] splitUsernameAndConnection(String combined) {
        return combined.split(":");
    }

    public static void userNotPresent(String roomName, String username, String connectionId) {
        try {
            Redis.zrem(getRedisPresenceKeyForRoom(roomName), getUsernameAndConnectionString(username, connectionId));
        } catch (Exception e) {
            Logger.error(e, "Error contacting redis.");
        }
    }

    private static void cleanPresenceSet(String roomName) {
        try {
            int time = (int) (System.currentTimeMillis() / 1000);
            Long removed = Redis.zremrangeByScore(getRedisPresenceKeyForRoom(roomName), 0, time - Constants.PRESENCE_TIMEOUT_SEC * 2);
//            Logger.debug("Clean of presence set for " + name + " removed " + removed + " elements.");
        } catch (Exception e) {
            Logger.error(e, "Error contacting redis.");
        }
    }

    public static long getCurrentUserCount(String roomName) {
        try {
            // Counting the set no longer works because of multiple connections
//            int time = (int) (System.currentTimeMillis() / 1000);
//            Long zcount = Redis.zcount(getRedisPresenceKeyForRoom(roomName), time - Constants.PRESENCE_TIMEOUT_SEC, time);
            TreeSet<String> usernamesPresent = getUsernamesPresent(roomName);
            return usernamesPresent.size();
        } catch (Exception e) {
            Logger.error("Error contacting redis.");
            return 0;
        }
    }

    public static TreeSet<String> getActiveRooms(int limit) {
        try {
            int time = (int) (System.currentTimeMillis() / 1000);
            Set<String> activeRoomNames = Redis.zrange(REDISKEY_ACTIVE_ROOMS, -limit, -1);
            return new TreeSet<String>(activeRoomNames);
        } catch (Exception e) {
            Logger.error(e, "Error contacting redis.");
            return new TreeSet<String>();
        }
    }

    public static void markRoomActive(String roomName) {
        try {
            int time = (int) (System.currentTimeMillis() / 1000);
            Redis.zadd(REDISKEY_ACTIVE_ROOMS, time, roomName);
            if (random.nextFloat() < CHANCE_CLEAN_REDIS_PRESENCE) {
                // this is just housekeeping to keep the sets from getting too big
                Long removed = Redis.zremrangeByRank(REDISKEY_ACTIVE_ROOMS, 0, -10);
                Logger.info("Remove " + removed + " active rooms from active room set.");
            }
        } catch (Exception e) {
            Logger.error(e, "Error contacting redis.");
        }
    }

}
