package jobs;

import models.ChatRoom;
import play.Logger;
import play.Play;
import play.jobs.Every;
import play.jobs.Job;

import java.util.List;

/**
 * Stress tester for dev mode message sending - just uncomment the schedule line below
 */
//@Every("1min")
public class MessageFreakoutForAllRoomsJob extends Job {

    @Override
    public void doJob() throws Exception {
        if (Play.mode.isDev()) {
            Logger.info("Starting chat room reddit bot job.");
            List<ChatRoom> allRooms = ChatRoom.findAll();
            for (ChatRoom chatRoom : allRooms) {
                new MessageFreakoutJob(chatRoom.getId()).now();
            }
        }
    }
}
