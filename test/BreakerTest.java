import models.ChatUser;
import org.junit.Before;
import play.Play;
import play.libs.Mail;
import play.test.Fixtures;
import play.test.UnitTest;

/**
 * Created by matt on 6/2/16.
 */
public abstract class BreakerTest extends UnitTest {

    @Before
    public void setUp() throws Exception {
        // todo Probably need to figure out test Redis database
        Fixtures.deleteDatabase();
        Mail.Mock.reset();
    }

    public ChatUser getUser() {
        ChatUser chatUser = new ChatUser("1");
        chatUser.username = "megamatt2000";
        // NOTE: These access tokens change every time to relogin into the site, need a better way to test
        chatUser.accessToken = Play.configuration.getProperty("reddit.matt.accesstoken");
        chatUser.refreshToken = Play.configuration.getProperty("reddit.matt.refreshtoken");
        chatUser.save();
        return chatUser;
    }

    public static ChatUser getTestUser1() {
        ChatUser chatUser = new ChatUser("1");
        chatUser.username = "breakerapptest1";
        chatUser.accessToken = Play.configuration.getProperty("reddit.testuser1.accesstoken");
        chatUser.refreshToken = Play.configuration.getProperty("reddit.testuser1.refreshtoken");
        chatUser.save();
        return chatUser;
    }

    public static ChatUser getTestUser2() {
        ChatUser chatUser = new ChatUser("2");
        chatUser.username = "breakerapptest2";
        chatUser.accessToken = Play.configuration.getProperty("reddit.testuser2.accesstoken");
        chatUser.refreshToken = Play.configuration.getProperty("reddit.testuser2.refreshtoken");
        chatUser.save();
        return chatUser;
    }
}
