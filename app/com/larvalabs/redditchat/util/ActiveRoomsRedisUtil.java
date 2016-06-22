package com.larvalabs.redditchat.util;

import com.larvalabs.redditchat.dataobj.JsonActiveChatRoom;
import org.apache.commons.codec.binary.Base64;
import play.Logger;
import play.modules.redis.Redis;

import java.io.*;
import java.util.Collections;
import java.util.List;

public class ActiveRoomsRedisUtil {

    private static final String ACTIVE_ROOMS_KEY = "ACTIVE_ROOMS";
    private static final int ACTIVE_ROOMS_EXPIRE = 600; // 10 minutes

    public static List<JsonActiveChatRoom> getActiveRooms() {
        String rooms = Redis.get(ACTIVE_ROOMS_KEY);
        if(rooms != null) {
            return deserialize(Base64.decodeBase64(rooms));
        }

        return Collections.emptyList();
    }

    public static String cacheActiveRooms(List<JsonActiveChatRoom> activeRoomsList) {
        byte[] activeRooms = serializeToBytes(activeRoomsList);
        return Redis.setex(ACTIVE_ROOMS_KEY, ACTIVE_ROOMS_EXPIRE, Base64.encodeBase64String(activeRooms));
    }

    private static byte[] serializeToBytes(List<JsonActiveChatRoom> list) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = null;
        try {
            oos = new ObjectOutputStream(bos);
            oos.writeObject(list);
            oos.flush();
            return bos.toByteArray();
        } catch (IOException e) {
            Logger.error(e, "Error converting active rooms list to byte array.");
            e.printStackTrace();
            return new byte[0];
        } finally {
            closeStream(oos);
        }
    }

    private static List<JsonActiveChatRoom> deserialize(byte[] activeRooms) {
        ByteArrayInputStream bis = new ByteArrayInputStream(activeRooms);
        ObjectInput in = null;
        try {
            in = new ObjectInputStream(bis);
            return (List<JsonActiveChatRoom>) in.readObject();
        } catch (IOException e) {
            Logger.error(e, "Error reading active rooms byte array.");
            e.printStackTrace();
            return Collections.emptyList();
        } catch (ClassNotFoundException e) {
            Logger.error(e, "Error converting active rooms byte array to list.");
            e.printStackTrace();
            return Collections.emptyList();
        } finally {
            closeStream(bis);
        }
    }

    private static void closeStream(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException ex) {
                // important exceptions already handled
            }
        }
    }
}
