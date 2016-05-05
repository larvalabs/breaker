package jobs;

import com.larvalabs.redditchat.dataobj.JsonUtil;
import com.larvalabs.redditchat.util.Util;
import models.ChatRoom;
import models.ChatUser;
import org.junit.Before;
import org.junit.Test;
import play.libs.Mail;
import play.test.Fixtures;
import play.test.UnitTest;

/**
 * Created by matt.
 */
public class PreloadStateTest extends UnitTest {

    @Before
    public void setUp() throws Exception {
        Fixtures.deleteDatabase();
        Mail.Mock.reset();
    }

    private ChatUser makeUser(String username) {
        ChatUser user = new ChatUser(Util.getShortRandomId());
        user.setUsername(username);
        user.save();
        return user;
    }

    private ChatRoom makeRoom(String name) {
        ChatRoom room = new ChatRoom(name);
        room.save();
        return room;
    }

    @Test
    public void testPreloadState() throws Exception {
        String connectionId = Util.getUUID();

        String username1 = "user1";
        ChatUser user1 = makeUser(username1);
        ChatUser user2 = makeUser("user2");
        ChatUser user3 = makeUser("User3");
        ChatUser user4 = makeUser("user4");
        String roomName1 = "room1";
        ChatRoom room1 = makeRoom(roomName1);
        String roomName2 = "Room2";     // Caps for first letter to verify sorting
        ChatRoom room2 = makeRoom(roomName2);

        user1.joinChatRoom(room1);
        user1.joinChatRoom(room2);
        room1.userPresent(user1.getUsername(), connectionId);
        room2.userPresent(user1.getUsername(), connectionId);

        user2.joinChatRoom(room1);
        room1.userPresent(user2.getUsername(), connectionId);

        user3.joinChatRoom(room2);
        room2.userPresent(user3.getUsername(), connectionId);

        // User4 is offline in both rooms
        user4.joinChatRoom(room1);
        user4.joinChatRoom(room2);
        room1.userNotPresent(user4.getUsername(), connectionId);
        room2.userNotPresent(user4.getUsername(), connectionId);
        room2.addModerator(user4);
        room2.refresh();        // Refresh other side of join on moderators so it's current

        {
            JsonUtil.FullState fullState = JsonUtil.loadFullStateForUser(user1);

            // check sorting
            assertEquals(username1, fullState.allUsers.firstEntry().getValue().username);
            assertEquals(roomName1, fullState.rooms.firstEntry().getValue().name);

            assertEquals(4, fullState.allUsers.size());
            assertEquals(2, fullState.rooms.size());

            assertEquals(2, fullState.members.get(roomName1).online.size());
            assertEquals(1, fullState.members.get(roomName1).offline.size());
            assertEquals(0, fullState.members.get(roomName1).mods.size());

            assertEquals(2, fullState.members.get(roomName2).online.size());
            assertEquals(0, fullState.members.get(roomName2).offline.size());
            assertEquals(1, fullState.members.get(roomName2).mods.size());
        }

        {
            JsonUtil.FullState fullState = JsonUtil.loadFullStateForUser(user2);
            assertEquals(3, fullState.allUsers.size());
            assertEquals(1, fullState.rooms.size());
        }
    }
}
