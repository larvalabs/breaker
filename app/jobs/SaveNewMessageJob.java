package jobs;

import com.larvalabs.linkunfurl.LinkInfo;
import com.larvalabs.linkunfurl.LinkUnfurl;
import com.larvalabs.redditchat.realtime.ChatRoomStream;
import com.larvalabs.redditchat.util.RedisUtil;
import com.larvalabs.redditchat.util.Util;
import models.ChatRoom;
import models.ChatUser;
import models.Message;
import models.WebLink;
import play.Logger;
import play.jobs.Job;

import java.io.IOException;
import java.util.Date;
import java.util.List;

/**
 * Created by matt on 12/23/15.
 */
public class SaveNewMessageJob extends Job<Message> {

    private String uuid;
    private String username;
    private String roomName;
    private String messageText;
    private Date createDate;
    private Message savedMessage;

    public SaveNewMessageJob(String uuid, String username, String roomName, String messageText, Date createDate) {
        this.uuid = uuid;
        this.username = username;
        this.roomName = roomName;
        this.messageText = messageText;
        this.createDate = createDate;
    }

    @Override
    public Message doJobWithResult() throws Exception {
        Logger.info("Saving message to room " + roomName);
        ChatRoom chatRoom = ChatRoom.findByName(roomName);
        ChatUser chatUser = ChatUser.findByUsername(username);
        savedMessage = new Message(uuid, chatUser, chatRoom, messageText);
        savedMessage.setCreateDate(createDate);
        savedMessage.unfurlLinks();
        savedMessage.save();

        ChatRoomStream.getEventStream(roomName).sendMessageUpdate(chatRoom, savedMessage);

        RedisUtil.markRoomActive(roomName);

        return savedMessage;
    }

    @Override
    public void after() {
        super.after();

        if (messageText.contains("@")) {
            new NotifyMentionedUsersJob(savedMessage.getId()).now();
        }
    }
}
