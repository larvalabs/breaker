import com.larvalabs.redditchat.util.RedisUtil;
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
        RedisUtil.clearAllKeys();
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

        room1.userPresent(user1.getUsername(), connectionId);
        assertEquals(1, room1.getUsernamesPresent().size());
        assertEquals(user1.getUsername(), room1.getUsernamesPresent().first());
        assertEquals(1, RedisUtil.getAllOnlineUsersForAllRooms().size());
        assertEquals(user1.getUsername(), RedisUtil.getAllOnlineUsersForAllRooms().first());

        room1.userNotPresent(user1.getUsername(), connectionId);
        RedisUtil.userNotPresentGlobal(user1.getUsername(), connectionId);
        assertEquals(0, room1.getUsernamesPresent().size());
        assertEquals(0, RedisUtil.getAllOnlineUsersForAllRooms().size());
    }

    @Test
    public void testMultipleConnectionsAndRooms() throws Exception {
        String connectionId1 = "testConnID1";
        String connectionId2 = "testConnID2";
        ChatUser user1 = new ChatUser("user1uid");
        user1.setUsername("user1");
        user1.save();

        ChatRoom room1 = new ChatRoom("testroom1");
        room1.save();
        user1.joinChatRoom(room1);
        ChatRoom room2 = new ChatRoom("testroom2");
        room2.save();
        user1.joinChatRoom(room2);

        room1.userPresent(user1.getUsername(), connectionId1);
        room2.userPresent(user1.getUsername(), connectionId1);
        room1.userPresent(user1.getUsername(), connectionId2);
        room2.userPresent(user1.getUsername(), connectionId2);

        assertEquals(1, RedisUtil.getAllOnlineUsersForAllRooms().size());
        assertEquals(1, RedisUtil.getCurrentUserCount(room1.getName()));
        assertEquals(1, RedisUtil.getCurrentUserCount(room2.getName()));

        // Disconnect a single connection to one room, one connection remains
        room1.userNotPresent(user1.getUsername(), connectionId1);
        assertEquals(1, RedisUtil.getAllOnlineUsersForAllRooms().size());
        assertEquals(1, RedisUtil.getCurrentUserCount(room1.getName()));
        assertEquals(1, RedisUtil.getCurrentUserCount(room2.getName()));

        // user 1 now leaving room 1 entirely
        room1.userNotPresent(user1.getUsername(), connectionId2);
        assertEquals(1, RedisUtil.getAllOnlineUsersForAllRooms().size());
        assertEquals(0, RedisUtil.getCurrentUserCount(room1.getName()));
        assertEquals(1, RedisUtil.getCurrentUserCount(room2.getName()));

        // conection 1 now disconnected
        room2.userNotPresent(user1.getUsername(), connectionId1);
        RedisUtil.userNotPresentGlobal(user1.getUsername(), connectionId1);
        assertEquals(1, RedisUtil.getAllOnlineUsersForAllRooms().size());
        assertEquals(0, RedisUtil.getCurrentUserCount(room1.getName()));
        assertEquals(1, RedisUtil.getCurrentUserCount(room2.getName()));

        room2.userNotPresent(user1.getUsername(), connectionId2);
        RedisUtil.userNotPresentGlobal(user1.getUsername(), connectionId2);
        assertEquals(0, RedisUtil.getAllOnlineUsersForAllRooms().size());
        assertEquals(0, RedisUtil.getCurrentUserCount(room1.getName()));
        assertEquals(0, RedisUtil.getCurrentUserCount(room2.getName()));

    }
}
