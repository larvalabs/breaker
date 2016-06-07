package jobs;

import models.ChatRoom;
import play.Logger;
import play.jobs.Job;
import reddit.BreakerRedditClient;

import java.util.List;

/**
 * Created by matt on 4/27/16.
 */
public class CheckIfSubredditExistsForRoomJob extends Job {

    public String roomName;

    public CheckIfSubredditExistsForRoomJob(String roomName) {
        this.roomName = roomName;
    }

    @Override
    public void doJob() throws Exception {
        BreakerRedditClient client = new BreakerRedditClient();
        if (!client.doesSubredditExist(roomName)) {
            Logger.info("Room "+roomName+" does not exist on Reddit, marking as deleted.");
            ChatRoom room = ChatRoom.findByName(roomName);
            room.setDeleted(true);
            room.save();
        }
    }
}
