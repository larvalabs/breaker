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
//@Every("15s")
public class MessageFreakoutForAllRoomsJob extends Job {

    private static int runcount = 0;

    @Override
    public void doJob() throws Exception {
        if (Play.mode.isDev()) {
            if (runcount < 10) {
                long runtime = System.currentTimeMillis();
                Logger.info("Starting chat room reddit bot job.");
                List<ChatRoom> allRooms = ChatRoom.findAll();
                for (ChatRoom chatRoom : allRooms) {
                    new MessageFreakoutJob(chatRoom.getId(), runtime).now();
                }
                runcount++;
            }
        }
    }
}
