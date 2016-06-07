package jobs;

import models.ChatRoom;
import models.ChatUser;
import play.Logger;
import play.jobs.Job;
import reddit.BreakerRedditClient;

import java.util.List;

/**
 * Created by matt on 4/27/16.
 */
public class MarkIncorrectRoomsDeletedJob extends Job {

    @Override
    public void doJob() throws Exception {
        Logger.info("Checking to see if rooms exist and marking deleted if not...");
        List<ChatRoom> allRooms = ChatRoom.findAll();
        int count = 0;
        for (ChatRoom room : allRooms) {
            new CheckIfSubredditExistsForRoomJob(room.getName()).now();
        }
    }
}
