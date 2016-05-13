import models.ChatRoom;
import models.ChatUser;
import org.junit.Test;
import play.test.UnitTest;

/**
 * Created by matt on 5/13/16.
 */
public class TestModels extends UnitTest {

    @Test
    public void testFindUserCaseInsensitive() throws Exception {
        String connectionId = "testConnID";
        ChatUser user1 = new ChatUser("user1uid");
        user1.setUsername("UserName");
        user1.save();
        assertEquals(user1, ChatUser.findByUsername("USERNAME"));
    }

    @Test
    public void testFindChatRoomCaseInsensitive() throws Exception {
        ChatRoom room1 = new ChatRoom("TestRoom");
        room1.save();
        assertEquals(room1, ChatRoom.findByName("TESTROOM"));
    }
}
