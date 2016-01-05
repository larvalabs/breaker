package jobs;

import models.ChatRoom;
import play.Logger;
import play.db.jpa.JPABase;
import play.jobs.Every;
import play.jobs.Job;

import java.util.List;

/**
 * Created by matt on 1/5/16.
 */
@Every("1min")
public class RedditLinksForAllRoomsJob extends Job {

    @Override
    public void doJob() throws Exception {
        Logger.info("Starting chat room reddit bot job.");
        List<ChatRoom> allRooms = ChatRoom.findAll();
        for (ChatRoom chatRoom : allRooms) {
            new RedditLinkBotJob(chatRoom.getName()).now();
        }
    }
}
