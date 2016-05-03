package jobs;

import com.amazonaws.util.json.JSONArray;
import com.amazonaws.util.json.JSONObject;
import com.larvalabs.redditchat.Constants;
import com.larvalabs.redditchat.dataobj.JsonChatRoom;
import com.larvalabs.redditchat.dataobj.JsonMessage;
import com.larvalabs.redditchat.dataobj.JsonUser;
import com.larvalabs.redditchat.realtime.ChatRoomStream;
import com.larvalabs.redditchat.util.Util;
import models.ChatRoom;
import models.ChatUser;
import models.Message;
import models.RedditLink;
import org.apache.commons.io.IOUtils;
import play.Logger;
import play.jobs.Every;
import play.jobs.Job;

import java.net.URL;
import java.net.URLConnection;
import java.util.List;

/**
 * Created by matt on 1/5/16.
 */
public class RedditLinkBotJob extends Job {

    private static final int MAX_NUM_TO_POST_PER_RUN = 1;

    private String subredditToProcess;

    public RedditLinkBotJob(String subredditToProcess) {
        this.subredditToProcess = subredditToProcess;
    }

    @Override
    public void doJob() throws Exception {
        Logger.debug("Starting link bot job for " + subredditToProcess);

        String subredditJsonUrl = "https://www.reddit.com/r/" + subredditToProcess + "/hot.json?sort=hot";
        URLConnection urlConnection = new URL(subredditJsonUrl).openConnection();
        // set user agent to avoid throttling
        urlConnection.setRequestProperty("User-Agent", "web:breakerapp:v0.1");
        String jsonStr = IOUtils.toString(urlConnection.getInputStream());

        JSONObject overallJsonObj = new JSONObject(jsonStr);
        JSONArray jsonArray = overallJsonObj.getJSONObject("data").getJSONArray("children");

        ChatUser botUser = ChatUser.getBreakerBot();
        ChatRoom room = ChatRoom.findByName(subredditToProcess);
        if (room == null || botUser == null) {
            Logger.error("Bot user or chat room was null, can't run bot.");
            return;
        }

        List<Message> messages = room.getMessages(1);
        if (messages != null && messages.size() > 0) {
            Message message = messages.get(0);
            if (message.getUser().equals(botUser)) {
                Logger.info("Linkbot not running for room " + subredditToProcess + " because last message is from link bot.");
                return;
            }
        }

        int numPosted = 0;

        for (int i = jsonArray.length() - 1; i >= 0; i--) {
            JSONObject itemObj = jsonArray.getJSONObject(i);
            String kind = itemObj.getString("kind");
            // t3 is link kind: https://www.reddit.com/dev/api
            if (kind.equals("t3")) {
                JSONObject itemData = itemObj.getJSONObject("data");
                String id = itemData.getString("id"); // with need name instead of id to make upvoats
                String title = itemData.getString("title");
                String subreddit = itemData.getString("subreddit");
                String url = itemData.getString("url");
                long score = itemData.getLong("score");
                long comments = itemData.getLong("num_comments");
//                    String permaLink = SITE_BASE_URL + itemData.getString("permalink");
                String permaLink = Constants.REDDIT_BASE_URL + itemData.getString("permalink");

                Logger.debug(id + ": " + title);

                RedditLink existingLink = RedditLink.findByRedditId(id);
                if (existingLink == null) {
                    Logger.debug("Link hasn't been posted, posting...");
                    // todo maybe store full json in the future, skip for now
                    RedditLink redditLink = new RedditLink(id, subreddit, permaLink, url, title, "");
                    redditLink.save();

                    Message message = new Message(botUser, room,
                            title + " - " + Constants.REDDIT_BASE_URL + "/" + id + " - score: " + score + " - comments: " + comments);
                    message.save();

                    ChatRoomStream.getEventStream(subredditToProcess).say(JsonMessage.from(message, botUser.getUsername(), room.getName()), JsonChatRoom.from(room), JsonUser.fromUser(message.getUser(), true));

                    numPosted++;
                    if (numPosted >= MAX_NUM_TO_POST_PER_RUN) {
                        Logger.debug("Hit max bot posts per run for " + subredditToProcess);
                        break;
                    }
                }
            }
        }

    }
}
