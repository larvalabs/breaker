import com.larvalabs.redditchat.ChatCommands;
import com.larvalabs.redditchat.dataobj.JsonChatRoom;
import com.larvalabs.redditchat.dataobj.JsonMessage;
import com.larvalabs.redditchat.dataobj.JsonUser;
import models.ChatRoom;
import models.ChatUser;
import org.junit.Before;
import org.junit.Test;
import play.Logger;
import play.libs.Mail;
import play.test.Fixtures;
import play.test.UnitTest;

/**
 * Created by matt on 12/24/15.
 */
public class MessageProcessingTest extends UnitTest {

    @Before
    public void setUp() throws Exception {
        Fixtures.deleteDatabase();
        Mail.Mock.reset();
    }

    @Test
    public void testConvertLinksInHtml() throws Exception {
        ChatUser user1 = new ChatUser("user1uid");
        user1.save();
        ChatRoom room1 = new ChatRoom("testroom");
        room1.save();
        JsonChatRoom jsonChatRoom = JsonChatRoom.from(room1);
        {
            JsonMessage testMsg = new JsonMessage("testuid", user1.getUsername(), jsonChatRoom.name,
                            "It's a test messasge to http://yahoo.com");
            Logger.info("Processed msg: " + testMsg.messageHtml);
            assertTrue(testMsg.messageHtml.contains("href"));
            assertEquals(1, testMsg.allLinks.length);
        }
        {
            JsonMessage testMsg = new JsonMessage("testuid", user1.getUsername(), jsonChatRoom.name,
                            "It's a test messasge to http://yahoo.com with image http://imgur.com/8543kjhdf.png");
            Logger.info("Processed msg: " + testMsg.messageHtml);
            assertTrue(testMsg.messageHtml.contains("href"));
            assertEquals(2, testMsg.allLinks.length);
            assertEquals(1, testMsg.imageLinks.length);
            assertEquals("http://imgur.com/8543kjhdf.png", testMsg.imageLinks[0]);
        }
    }

    @Test
    public void testCommandHandling() throws Exception {
        ChatUser user1 = new ChatUser("user1uid");
        user1.save();
        ChatRoom room1 = new ChatRoom("testroom");
        room1.save();
        user1.joinChatRoom(room1);

        String message = "/ban @user1uid ";
        assertTrue(ChatCommands.isCommand(message));
        ChatCommands.Command command = ChatCommands.getCommand(message);
        assertTrue(command.type == ChatCommands.CommandType.BAN);
        assertEquals("user1uid", command.username);
//        ChatCommands.execCommand(room1, message, null, null);
    }
}
