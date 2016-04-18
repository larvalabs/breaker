import models.ChatRoom;
import models.ChatUser;
import org.junit.Before;
import org.junit.Test;
import play.libs.Mail;
import play.test.Fixtures;
import play.test.UnitTest;

/**
 * Created by matt on 4/17/16.
 */
public class LastMessageReadTest extends UnitTest {

    @Before
    public void setUp() throws Exception {
        // todo Probably need to figure out test Redis database
        Fixtures.deleteDatabase();
        Mail.Mock.reset();
    }

    @Test
    public void testRoomAndGlobalPresence() throws Exception {
        String connectionId = "testConnID";
        ChatUser user1 = new ChatUser("user1uid");
        user1.setUsername("user1" + System.currentTimeMillis());
        user1.save();
        ChatRoom room1 = new ChatRoom("testroom");
        room1.save();
        user1.joinChatRoom(room1);

        long startTime = System.currentTimeMillis();
        assertEquals(0, room1.getLastMessageReadTimeForUser(user1));
        room1.markMessagesReadForUser(user1);
        assertTrue(room1.getLastMessageReadTimeForUser(user1) > startTime);
    }
}
