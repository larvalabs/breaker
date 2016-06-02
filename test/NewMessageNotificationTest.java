import com.larvalabs.redditchat.Constants;
import jobs.NotifyNewMessagesJob;
import jobs.SaveLastReadForUserJob;
import jobs.SaveLastReadTimeForAllPendingJob;
import models.ChatRoom;
import models.ChatUser;
import models.Message;
import org.junit.Before;
import org.junit.Test;
import play.db.jpa.JPA;
import play.libs.Mail;
import play.test.Fixtures;
import play.test.UnitTest;

/**
 * Created by matt on 4/17/16.
 */
public class NewMessageNotificationTest extends BreakerTest {

    @Test
    public void testSingleRoomNewMessageNotification() throws Exception {
        String connectionId = "testConnID";

        ChatUser botUser = new ChatUser("botuserid");
        botUser.setUsername(Constants.BREAKER_BOT_USERNAME);
        botUser.setBot(true);
        botUser.save();
        ChatUser user1 = getTestUser1();
        user1.setNotificationPreference(ChatUser.PREFVAL_NOTIFICATION_MENTIONED);
        user1.save();
        ChatUser user2 = getTestUser2();

        ChatRoom room1 = new ChatRoom("testroom");
        room1.save();
        user1.joinChatRoom(room1);
        user2.joinChatRoom(room1);
        room1.markMessagesReadForUser(user1.getUsername());
//        user1.markAllRoomsAsRead();
        user1.refresh();
        room1.markMessagesReadForUser(user2.getUsername());
        user2.refresh();

        new SaveLastReadForUserJob(user2.getUsername(), room1.getName()).doJob();

        {
            Message message = new Message(user1, room1, "New message.");
            message.save();
        }

        {
            NotifyNewMessagesJob.NewMessageInfo messageInfo = new NotifyNewMessagesJob.NewMessageInfo(user2.getChatRoomJoins()).invoke();
            assertEquals(1, messageInfo.getTotalNewMessages());
            assertEquals(1, messageInfo.getRoomsWithNewMessages().size());
            assertEquals(room1, messageInfo.getRoomsWithNewMessages().get(0));
        }

        // This is normally done async in the server so we don't overwrite to the DB, but I'll force it here to test
        room1.markMessagesReadForUser(user2.getUsername());
        new SaveLastReadForUserJob(user2.getUsername(), room1.getName()).doJob();
        user2.refresh();

        JPA.em().flush();

        {
            NotifyNewMessagesJob.NewMessageInfo messageInfo = new NotifyNewMessagesJob.NewMessageInfo(user2.getChatRoomJoins()).invoke();
            assertEquals(0, messageInfo.getTotalNewMessages());
            assertEquals(0, messageInfo.getRoomsWithNewMessages().size());
        }

        {
            Message message = new Message(botUser, room1, "BOT New message, should be ignored for new counts.");
            message.save();
        }

//        JPA.em().flush();

        {
            NotifyNewMessagesJob.NewMessageInfo messageInfo = new NotifyNewMessagesJob.NewMessageInfo(user2.getChatRoomJoins()).invoke();
            assertEquals(0, messageInfo.getTotalNewMessages());
            assertEquals(0, messageInfo.getRoomsWithNewMessages().size());
        }

        {
            Message message = new Message(user1, room1, "New message 3.");
            message.save();
        }
        {
            Message message = new Message(user1, room1, "New message 4.");
            message.save();
        }

//        JPA.em().flush();

        {
            NotifyNewMessagesJob.NewMessageInfo messageInfo = new NotifyNewMessagesJob.NewMessageInfo(user2.getChatRoomJoins()).invoke();
            assertEquals(2, messageInfo.getTotalNewMessages());
            assertEquals(1, messageInfo.getRoomsWithNewMessages().size());
            assertEquals(room1, messageInfo.getRoomsWithNewMessages().get(0));
        }

        // Note if executing a job async you need to commit and open a new transaction, leaving here for reference
//        JPA.em().getTransaction().commit();
//        JPA.em().getTransaction().begin();

        {
            // Assert that a message would NOT be sent to user1 because of their notification pref setting
            assertFalse(new NotifyNewMessagesJob(user1.getId()).doJobWithResult());
        }
    }
}
