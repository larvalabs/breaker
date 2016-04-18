package jobs;

import models.ChatRoom;
import models.ChatUser;
import models.ChatUserRoomJoin;
import play.Logger;
import play.jobs.Job;

/**
 * Created by matt on 4/17/16.
 */
public class SaveLastReadForUserJob extends Job {

    private String username;
    private String roomName;

    public SaveLastReadForUserJob(String username, String roomName) {
        this.username = username;
        this.roomName = roomName;
    }

    @Override
    public void doJob() throws Exception {
        ChatUser user = ChatUser.findByUsername(username);
        ChatRoom room = ChatRoom.findByName(roomName);
        ChatUserRoomJoin join = ChatUserRoomJoin.findByUserAndRoom(user, room);
        long lastMessageReadTimeForUser = room.getLastMessageReadTimeForUser(user);
        Logger.info("Settings last message read for " + user.getUsername() + " in " + room.getName() + " to " + lastMessageReadTimeForUser);
        join.setLastSeenMessageTime(lastMessageReadTimeForUser);
        join.save();
    }
}
