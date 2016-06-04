package jobs;

import com.larvalabs.redditchat.dataobj.JsonChatRoom;
import com.larvalabs.redditchat.dataobj.JsonMessage;
import com.larvalabs.redditchat.dataobj.JsonUser;
import com.larvalabs.redditchat.realtime.ChatRoomStream;
import com.larvalabs.redditchat.util.Util;
import models.ChatRoom;
import models.ChatUser;
import play.Logger;
import play.Play;
import play.jobs.Every;
import play.jobs.Job;

import java.util.List;

/**
 * Created by matt on 1/5/16.
 */
public class MessageFreakoutJob extends Job {

    public long runtime;
    public Long roomId;

    public MessageFreakoutJob(Long roomId, long runtime) {
        this.roomId = roomId;
        this.runtime = runtime;
    }

    @Override
    public void doJob() throws Exception {
        if (Play.mode.isDev()) {
            ChatRoom room = ChatRoom.findById(roomId);
            String megamatt2000 = "megamatt2000";
            ChatUser user = ChatUser.findByUsername(megamatt2000);
            JsonUser jsonUser = JsonUser.fromUser(user, true);
            JsonChatRoom jsonChatRoom = JsonChatRoom.from(room);
            Logger.info("Message freakout for room " + room.getName());
            for (int i = 0; i < 5; i++) {
                JsonMessage jsonMessage = JsonMessage.makePresavedMessage(Util.getUUID(), megamatt2000, room.getName(), "Message " + i + " at " + runtime);
                ChatRoomStream.getEventStream(room.getName()).say(jsonMessage, jsonChatRoom, jsonUser);
            }
        }
    }
}
