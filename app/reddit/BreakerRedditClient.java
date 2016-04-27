package reddit;

import com.amazonaws.util.json.JSONException;
import com.amazonaws.util.json.JSONObject;
import com.google.gson.JsonElement;
import com.larvalabs.redditchat.Constants;
import models.ChatUser;
import org.apache.commons.io.IOUtils;
import play.Logger;
import play.libs.WS;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.MessageFormat;
import java.util.HashMap;

public class BreakerRedditClient{
    private final String BREAKER_USER_AGENT = "web:breakerapp:v0.1";
    final private String OAUTH_URL = "https://oauth.reddit.com";
    final private String USER_PREF_URL = OAUTH_URL + "/api/v1/me/prefs";
    final private String USER_FLAIR_URL = OAUTH_URL + "/r/{0}/api/flairselector";
    final private String REFRESH_TOKEN_URL = OAUTH_URL + "/api/v1/access_token";

    public BreakerRedditClient(){
    }

    public JSONObject getRedditUserFlairForSubreddit(ChatUser chatUser, String subreddit) throws Exception{
        return postJsonForUser(chatUser, MessageFormat.format(USER_FLAIR_URL, subreddit));
    }

    private JSONObject getJsonForUser(ChatUser chatUser, String endpoint) throws Exception{
        return fetchJsonForUser(chatUser, endpoint, "GET", true);
    }

    private JSONObject postJsonForUser(ChatUser chatUser, String endpoint) throws Exception{
        return fetchJsonForUser(chatUser, endpoint, "POST", true);
    }

    private JSONObject fetchJsonForUser(ChatUser chatUser, String endpoint, String method, boolean refreshToken) throws Exception{
       try{
           return fetchJson(endpoint, method, chatUser.accessToken);
       } catch (TokenRefreshNeeded t){
           if (refreshToken){
               chatUser.accessToken = refreshToken(chatUser.refreshToken);
               chatUser.save();
               return fetchJsonForUser(chatUser, endpoint, method, false);
           } else {
               throw new Exception("Not able to auth");
           }
       }
    }

    private JSONObject fetchJson(String endpoint, String method, String bearer) throws TokenRefreshNeeded{
        return fetchJson(endpoint, method, bearer, null);
    }

    private JSONObject getJson(String endpoint, String method, String bearer, HashMap<String, String> params) throws TokenRefreshNeeded{
        return fetchJson(endpoint, "GET", bearer, params);
    }

    private JSONObject postJson(String endpoint, String bearer, HashMap<String, String> params) throws TokenRefreshNeeded{
        return fetchJson(endpoint, "POST", bearer, params);
    }

    private JSONObject fetchJson(String endpoint, String method, String bearer, HashMap<String, String> params) throws TokenRefreshNeeded{
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

            int code = conn.getResponseCode();

            if(code == 401){
                throw new TokenRefreshNeeded();
            } else if(code != 200){
                throw new Exception("Not 200");
            }

            String jsonStr = IOUtils.toString(conn.getInputStream());
            return new JSONObject(jsonStr);
        } catch (IOException | JSONException e){
            e.printStackTrace();
        } catch (TokenRefreshNeeded t){
            throw t;
        } catch (Exception e){
            e.printStackTrace();
        }

        return null;
    }

    public String refreshToken(String refreshToken) throws Exception{
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
