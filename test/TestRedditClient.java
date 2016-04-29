import com.amazonaws.util.json.JSONObject;
import com.larvalabs.redditchat.Constants;
import models.ChatUser;
import org.junit.Before;
import org.junit.Test;
import play.Logger;
import play.libs.Mail;
import play.test.Fixtures;
import play.test.UnitTest;
import reddit.BreakerRedditClient;

import java.util.ArrayList;
import java.util.List;

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

    private ChatUser getUser() {
        ChatUser chatUser = new ChatUser("1");
        chatUser.username = "megamatt2000";
        // NOTE: These access tokens change every time to relogin into the site, need a better way to test
        chatUser.accessToken = "***REMOVED***";
        chatUser.refreshToken = "***REMOVED***";
        return chatUser;
    }

    @Test
    public void testRefreshToken() throws Exception {
        ChatUser chatUser = getUser();

        // Refresh access token for user
        BreakerRedditClient breakerRedditClient = new BreakerRedditClient();
        String newToken = breakerRedditClient.refreshToken(chatUser.refreshToken);

        Logger.info("Received new token: " + newToken);
        assertNotNull(newToken);

    }

    @Test
    public void testGetFlair() throws Exception {
        ChatUser chatUser = getUser();

        // Refresh access token for user
        BreakerRedditClient breakerRedditClient = new BreakerRedditClient();
        JSONObject hockeyFlair = breakerRedditClient.getRedditUserFlairForSubreddit(chatUser, "hockey");

        Logger.info("Received flair: " + hockeyFlair.toString());
    }

    @Test
    public void testGetSubsModerated() throws Exception {
        ChatUser chatUser = getUser();

        // Refresh access token for user
        BreakerRedditClient breakerRedditClient = new BreakerRedditClient();
        ArrayList<String> subNamesModerated = breakerRedditClient.getSubNamesModerated(chatUser);
        assertEquals(3, subNamesModerated.size());
        assertTrue(subNamesModerated.contains("appchat"));
        assertTrue(subNamesModerated.contains("breakerapp"));
    }

    @Test
    public void testGetModerators() throws Exception {
        BreakerRedditClient client = new BreakerRedditClient();
        List<String> moderatorUsernames = client.getModeratorUsernames(Constants.CHATROOM_DEFAULT);
        assertEquals(3, moderatorUsernames.size());
        assertTrue(moderatorUsernames.contains("megamatt2000"));
        assertTrue(moderatorUsernames.contains("pents900"));
        assertTrue(moderatorUsernames.contains("rickiibeta"));
    }
}
