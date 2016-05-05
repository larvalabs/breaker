package reddit;

import com.amazonaws.util.json.JSONException;
import com.amazonaws.util.json.JSONObject;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.larvalabs.redditchat.Constants;
import models.ChatUser;
import org.apache.commons.io.IOUtils;
import play.Logger;
import play.libs.WS;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.*;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class BreakerRedditClient{
    private final String BREAKER_USER_AGENT = "web:breakerapp:v0.1";
    final private String OAUTH_URL = "https://oauth.reddit.com";
    final private String BASIC_URL = "https://www.reddit.com";
    final private String USER_PREF_URL = OAUTH_URL + "/api/v1/me/prefs";
    final private String USER_FLAIR_URL = OAUTH_URL + "/r/{0}/api/flairselector";
    final private String USER_SUBS_MODERATED_URL = OAUTH_URL + "/subreddits/mine/moderator/.json";
    final private String REFRESH_TOKEN_URL = OAUTH_URL + "/api/v1/access_token";
    final private String MODERATORS_URL = BASIC_URL + "/r/{0}/about/moderators.json";
    final private String ABOUT_URL = BASIC_URL + "/r/{0}/about.json";
    final private String ABOUT_URL_LOGGED_IN = OAUTH_URL + "/r/{0}/about";

    public static class RedditJsonUserlist {
        public RedditJsonUserData data;
    }

    public static class RedditJsonUserData {
        public RedditJsonUser[] children;
    }

    public static class RedditJsonUser {
        public String name;
        public String id;
    }

    public static class RedditJsonSubRedditList {
        public RedditJsonSubredditData data;
    }

    public static class RedditJsonSubredditData {
        public RedditJsonSubredditChild[] children;
    }

    public static class RedditJsonSubredditChild {
        public RedditJsonSubreddit data;
    }

    public static class RedditJsonSubreddit {
        public String name; // actually ID
        public String display_name;
    }

    public BreakerRedditClient(){
    }

    public boolean isSubredditPrivate(String subreddit) {
        String url = MessageFormat.format(ABOUT_URL, subreddit);
        WS.HttpResponse response = WS.url(url)
                .setHeader("User-Agent", this.BREAKER_USER_AGENT)
                .get();
        if (response.success()) {
            return false;
        }
        return true;
    }

    public boolean doesUserHaveAccessToSubreddit(ChatUser user, String subreddit) {
        try {
            JSONObject jsonResponse = getJsonForUser(user, MessageFormat.format(ABOUT_URL_LOGGED_IN, subreddit));
            if (jsonResponse == null) {
                return false;
            }
            if (jsonResponse.has("error")) {
                try {
                    if (jsonResponse.getInt("error") == 403) {
                        return false;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    return false;
                }
            }
            return true;
        } catch (RedditRequestError redditRequestError) {
            Logger.info("Unable to access subreddit info, user does not have access.");
            return false;
        }

    }

    public List<String> getModeratorUsernames(String subreddit) throws IOException {
        String url = MessageFormat.format(MODERATORS_URL, subreddit);
//            System.setProperty("http.agent", BREAKER_USER_AGENT);
//            String response = IOUtils.toString(new URI(url));

        String response = WS.url(url)
                .setHeader("User-Agent", this.BREAKER_USER_AGENT)
                .get().getString();

        ArrayList<String> usernames = new ArrayList<>();
        RedditJsonUserlist redditJsonUserlist = new Gson().fromJson(response, RedditJsonUserlist.class);
        for (RedditJsonUser child : redditJsonUserlist.data.children) {
            usernames.add(child.name);
        }
        return usernames;
    }

    public JSONObject getRedditUserFlairForSubreddit(ChatUser chatUser, String subreddit) throws RedditRequestError{
        return postJsonForUser(chatUser, MessageFormat.format(USER_FLAIR_URL, subreddit));
    }

    public JSONObject getSubsModerated(ChatUser chatUser) throws RedditRequestError {
        return getJsonForUser(chatUser, USER_SUBS_MODERATED_URL);
    }

    public ArrayList<String> getSubNamesModerated(ChatUser chatUser) throws RedditRequestError {
        JSONObject subsModerated = getSubsModerated(chatUser);
        // todo This is dumb to convert to json then back to string, fix
        RedditJsonSubRedditList redditJsonSubRedditList = new Gson().fromJson(subsModerated.toString(), RedditJsonSubRedditList.class);
        ArrayList<String> subnames = new ArrayList<>();
        for (RedditJsonSubredditChild child : redditJsonSubRedditList.data.children) {
            subnames.add(child.data.display_name);
        }
        return subnames;
    }

    private JSONObject getJsonForUser(ChatUser chatUser, String endpoint) throws RedditRequestError{
        return fetchJsonForUser(chatUser, endpoint, "GET", true);
    }

    private JSONObject postJsonForUser(ChatUser chatUser, String endpoint) throws RedditRequestError{
        return fetchJsonForUser(chatUser, endpoint, "POST", true);
    }

    private JSONObject fetchJsonForUser(ChatUser chatUser, String endpoint, String method, boolean refreshToken) throws RedditRequestError{
       try{
           return fetchJson(endpoint, method, chatUser.accessToken);
       } catch (TokenRefreshNeeded t){
           if (refreshToken){
               chatUser.accessToken = refreshToken(chatUser.refreshToken);
               chatUser.save();
               return fetchJsonForUser(chatUser, endpoint, method, false);
           } else {
               throw new RedditRequestError();
           }
       }
    }

    private JSONObject fetchJson(String endpoint, String method, String bearer) throws RedditRequestError{
        return fetchJson(endpoint, method, bearer, null);
    }

    private JSONObject getJson(String endpoint, String method, String bearer, HashMap<String, String> params) throws RedditRequestError{
        return fetchJson(endpoint, "GET", bearer, params);
    }

    private JSONObject postJson(String endpoint, String bearer, HashMap<String, String> params) throws RedditRequestError{
        return fetchJson(endpoint, "POST", bearer, params);
    }

    private JSONObject fetchJson(String endpoint, String method, String bearer, HashMap<String, String> params) throws RedditRequestError{
        try{
            final HttpURLConnection conn = (HttpURLConnection) new URL(endpoint).openConnection();

            // set headers
            if(bearer != null){
                conn.setRequestProperty("Authorization", "Bearer " + bearer);
            }

            // set user agent to avoid throttling
            conn.setRequestProperty("User-Agent", this.BREAKER_USER_AGENT);

            conn.setRequestMethod(method);

            if(method.equals("POST")){
                conn.setReadTimeout(10000);
                conn.setConnectTimeout(15000);
                conn.setDoInput(true);
                conn.setDoOutput(true);

                String query = getQuery(params);
                conn.setRequestProperty("Content-Length", "" + Integer.toString(query.getBytes("UTF-8").length));
                conn.getOutputStream().write(query.getBytes());
            }

            conn.connect();
            String jsonStr = IOUtils.toString(conn.getInputStream());

            int code = conn.getResponseCode();

            if(code == 401){
                throw new TokenRefreshNeeded();
            } if (code == 403) {
                throw new InvalidAuthScope();
            } if(code != 200){
                throw new RedditRequestError("Received error code: " + code + ", response: " + jsonStr);
            }

            return new JSONObject(jsonStr);
        } catch (IOException | JSONException e){
            e.printStackTrace();
        } catch (TokenRefreshNeeded t){
            throw t;
        }

        return null;
    }

    public String refreshToken(String refreshToken){
        final HashMap<String, String> params = new HashMap<String, String>();
        params.put("grant_type", "refresh_token");
        params.put("refresh_token", refreshToken);

        WS.HttpResponse res = WS.url("https://www.reddit.com/api/v1/access_token")
                .setHeader("Content-Type", "application/x-www-form-urlencoded")
                .setHeader("User-Agent", this.BREAKER_USER_AGENT)
                .setHeader("Authorization", "Basic " + Constants.REDDIT_AUTH_STR)
                .setParameters(params)
                .post();

        int status = res.getStatus();
        if (status == 200) {
            String type = res.getContentType();
//            String content = res.getString();
            JsonElement json = res.getJson();
            Logger.info("Received: " + json.toString());
            return json.getAsJsonObject().get("access_token").getAsString();
        } else {
            return null;
        }
    }

    private String getQuery(HashMap<String, String> params) throws UnsupportedEncodingException
    {
        if(params == null){
            return "";
        }

        StringBuilder result = new StringBuilder();
        boolean first = true;

        for (String key : params.keySet())
        {
            if(!first){
                result.append("&");
            }
            first = false;
            result.append(URLEncoder.encode(key, "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(params.get(key), "UTF-8"));

        }

        return result.toString();
    }
}
