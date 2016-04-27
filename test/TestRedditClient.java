import models.ChatUser;
import org.junit.Before;
import org.junit.Test;
import play.Logger;
import play.libs.Mail;
import play.test.Fixtures;
import play.test.UnitTest;
import reddit.BreakerRedditClient;

/**
 * Created by matt on 4/27/16.
 */
public class TestRedditClient extends UnitTest {

    @Before
    public void setUp() throws Exception {
        // todo Probably need to figure out test Redis database
        Fixtures.deleteDatabase();
        Mail.Mock.reset();
    }

    @Test
    public void testRefreshToken() throws Exception {
        ChatUser chatUser = new ChatUser("1");
        chatUser.username = "megamatt2000";
        chatUser.accessToken = "38478176-8ryTZfcMXEw5zYx0qkRwY7duRYk";
        chatUser.refreshToken = "38478176-tzgBE8fUaa5Rth7_cYkmbPKhLDk";

        // Refresh access token for user
        BreakerRedditClient breakerRedditClient = new BreakerRedditClient();
        String newToken = breakerRedditClient.refreshToken(chatUser.refreshToken);

        Logger.info("Received new token: " + newToken);
        assertNotNull(newToken);

    }
}
