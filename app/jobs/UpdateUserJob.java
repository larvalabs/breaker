package jobs;

import models.ChatUser;
import play.jobs.Job;
import play.jobs.OnApplicationStart;
import reddit.BreakerRedditClient;

/**
 * Created by matt on 1/5/16.
 */
@OnApplicationStart
public class UpdateUserJob extends Job {

    @Override
    public void doJob() throws Exception {
        // get user from reddit
        // save flair
        // Mock user
        ChatUser chatUser = new ChatUser("1");
        chatUser.username = "mathent";
        chatUser.accessToken = "9567379-IQp3H3AeY0XM2ci-E5NzotmGMHU";
        chatUser.refreshToken = "9567379-7ARi2_GnU49mCpJUIzbGBmrCKpk";

        // Refresh access token for user
        BreakerRedditClient breakerRedditClient = new BreakerRedditClient();
        String newToken = breakerRedditClient.refreshTokenWS(chatUser.refreshToken);

        System.out.print(newToken);
    }
}
