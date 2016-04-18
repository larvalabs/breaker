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
public class UserPresenceTest extends UnitTest {

    @Before
    public void setUp() throws Exception {
        Fixtures.deleteDatabase();
        Mail.Mock.reset();
    }

    @Test
    public void testRoomAndGlobalPresence() throws Exception {
        String connectionId = "testConnID";
        ChatUser user1 = new ChatUser("user1uid");
        user1.setUsername("user1");
        user1.save();
        ChatRoom room1 = new ChatRoom("testroom");
        room1.save();
        user1.joinChatRoom(room1);

        room1.userPresent(user1, connectionId);
        assertEquals(1, room1.getUsernamesPresent().size());
        assertEquals(user1.getUsername(), room1.getUsernamesPresent().first());
        assertEquals(1, ChatRoom.getAllOnlineUsersForAllRooms().size());
        assertEquals(user1.getUsername(), ChatRoom.getAllOnlineUsersForAllRooms().first());

        room1.userNotPresent(user1, connectionId);
        assertEquals(0, room1.getUsernamesPresent().size());
        assertEquals(0, ChatRoom.getAllOnlineUsersForAllRooms().size());
    }
}
