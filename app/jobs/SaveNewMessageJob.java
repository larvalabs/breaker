package jobs;

import models.ChatRoom;
import models.ChatUser;
import models.Message;
import play.Logger;
import play.jobs.Job;

/**
 * Created by matt on 12/23/15.
 */
public class SaveNewMessageJob extends Job<Message> {

    private String uuid;
    private ChatUser user;
    private String roomName;
    private String messageText;

    public SaveNewMessageJob(String uuid, ChatUser user, String roomName, String messageText) {
        this.uuid = uuid;
        this.user = user;
        this.roomName = roomName;
        this.messageText = messageText;
    }

    @Override
    public Message doJobWithResult() throws Exception {
        Logger.info("Saving message...");
        ChatRoom chatRoom = ChatRoom.findByName(roomName);
        Message message = new Message(uuid, user, chatRoom, messageText);
        message.save();
        return message;
    }
}
