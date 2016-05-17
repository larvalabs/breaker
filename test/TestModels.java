import models.ChatRoom;
import models.ChatUser;
import models.Message;
import org.junit.Test;
import play.test.UnitTest;

import java.util.List;

/**
 * Created by matt on 5/13/16.
 */
public class TestModels extends UnitTest {

    public static Message makeMessage(ChatUser user, ChatRoom room, String msgText) {
        Message message = new Message(user, room, msgText);
        message.save();
        return message;
    }

    public static ChatRoom makeRoom(String name) {
        ChatRoom chatRoom = new ChatRoom(name);
        chatRoom.save();
        return chatRoom;
    }

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

    @Test
    public void testFindBeforeMessageTime() throws Exception {
        ChatRoom chatRoom = makeRoom("test1");
        ChatUser testUser1 = TestRedditClient.getTestUser1();
        Message message1 = makeMessage(testUser1, chatRoom, "Test1");
        Message message2 = makeMessage(testUser1, chatRoom, "Test2");
        Message message3 = makeMessage(testUser1, chatRoom, "Test3");

        {
            List<Message> messages = chatRoom.getMessagesWithoutBannedBefore(testUser1, message2.getId(), 10);
            assertEquals(1, messages.size());
            assertEquals("Test1", messages.get(0).getMessageText());
        }
        {
            List<Message> messages = chatRoom.getMessagesWithoutBannedAfter(testUser1, message2.getId(), 10);
            assertEquals(1, messages.size());
            assertEquals("Test3", messages.get(0).getMessageText());
        }
    }
}
