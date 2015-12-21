package controllers;

import com.google.gson.JsonObject;
import org.apache.commons.codec.binary.Base64;
import play.*;
import play.libs.OAuth2;
import play.libs.WS;
import play.mvc.*;
import play.data.validation.*;

import java.util.*;

import models.*;

public class Application extends Controller {

    public static final String REDDIT_CLIENTID = Play.configuration.getProperty("oauth.reddit.clientid");
    public static final String REDDIT_SECRET = Play.configuration.getProperty("oauth.reddit.secret");
    public static final String REDDIT_CALLBACK = Play.configuration.getProperty("oauth.reddit.callbackurl");
    public static final String REDDIT_SCOPE = WS.encode("identity,mysubreddits");
    public static final String REDDIT_AUTHORIZATION_URL = "https://www.reddit.com/api/v1/authorize?client_id=" + REDDIT_CLIENTID + "&response_type=code" +
            "&state=iuknjvdihu&redirect_uri=" + REDDIT_CALLBACK + "&duration=permanent&scope=" + REDDIT_SCOPE;
    public static final String REDDIT_TOKEN_URL = "https://www.reddit.com/api/v1/access_token";

    public static final String OAUTH_API_DOMAIN = "https://oauth.reddit.com";
    public static final String ENDPOINT_ME = OAUTH_API_DOMAIN + "/api/v1/me";
    public static final String COOKIE_CHATUSERNAME = "chatusername";
    public static final String COOKIE_CHATTOKEN = "chattoken";

    @Before
    static void setUser() {
        ChatUser user = null;
        if (session.contains("uid")) {
            Logger.info("existing user: " + session.get("uid"));
            user = ChatUser.get(Long.parseLong(session.get("uid")));
        }
        if (user == null) {
            user = ChatUser.createNew();
            session.put("uid", user.uid);
        }
        renderArgs.put("user", user);
    }

    private static ChatUser connected() {
        return (ChatUser) renderArgs.get("user");
    }

    public static void index() {
        ChatUser chatUser = connected();
        if (chatUser.accessToken == null) {
            auth();
            return;
        }

        WebSocket.room(chatUser.username);
//        render();
    }

    public static void auth() {
        Logger.debug("Received auth response.");
        if (OAuth2.isCodeResponse()) {
            ChatUser u = connected();
//            OAuth2.Response response = retrieveAccessToken(REDDIT_CALLBACK);
            Tokens tokens = retrieveAccessToken(REDDIT_CALLBACK);
            u.accessToken = tokens.access;
            u.refreshToken = tokens.refresh;
            String uuid = UUID.randomUUID().toString();
            u.localToken = uuid.substring(uuid.lastIndexOf("-") + 1, uuid.length());
            u.save();

            if (u != null && u.accessToken != null) {

//            Http.Header[] headers = new Http.Header[2];
                HashMap<String, String> headers = getOauthHeaders(u.accessToken);
                JsonObject me = WS.url(ENDPOINT_ME)
                        .headers(headers)
                        .get().getJson().getAsJsonObject();

                String username = me.get("name").getAsString();
                u.username = username;
                u.linkKarma = me.get("link_karma").getAsLong();
                u.commentKarma = me.get("link_karma").getAsLong();
                u.lastResponseApiMe = me.toString();
                u.save();
            }

            index();
        }
        redirect(REDDIT_AUTHORIZATION_URL);
//        REDDIT.retrieveVerificationCode(authURL());
    }

    private static class Tokens {
        String access;
        String refresh;

        private Tokens(String access, String refresh) {
            this.access = access;
            this.refresh = refresh;
        }
    }

    private static Tokens retrieveAccessToken(String callbackURL) {
        String accessCode = Scope.Params.current().get("code");
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("client_id", REDDIT_CLIENTID);
        params.put("client_secret", REDDIT_SECRET);
        params.put("grant_type", "authorization_code");
        params.put("redirect_uri", callbackURL);
        params.put("code", accessCode);
        HashMap<String, String> headers = getOauthHeaders(accessCode);
        String authStr = REDDIT_CLIENTID + ":" + REDDIT_SECRET;
        headers.put("Authorization", "Basic " + Base64.encodeBase64String(authStr.getBytes()));

        WS.HttpResponse response = WS.url(REDDIT_TOKEN_URL).headers(headers).params(params).post();
        JsonObject jsonObject = response.getJson().getAsJsonObject();
        Logger.debug("JSON resp: " + jsonObject.toString());
        Logger.debug("JSON resp: " + jsonObject.get("access_token").toString());
        return new Tokens(jsonObject.get("access_token").getAsString(), jsonObject.get("refresh_token").getAsString());
//        return jsonObject.get("accessToken").getAsString();
//        Logger.debug("Response: " + response);
//        return new OAuth2.Response(response);
    }

    private static HashMap<String, String> getOauthHeaders(String token) {
        HashMap<String, String> headers = new HashMap<String, String>();
        headers.put("User-Agent", "chat/0.1 by megamatt2000");
        headers.put("Authorization", "bearer " + WS.encode(token));
        return headers;
    }

    public static void enterDemo(@Required String user, @Required String demo) {
        if (validation.hasErrors()) {
            flash.error("Please choose a nick name and the demonstration type…");
            index();
        }

        // Dispatch to the demonstration        
        if (demo.equals("refresh")) {
            Refresh.index(user);
        }
        if (demo.equals("longpolling")) {
            LongPolling.room(user);
        }
        if (demo.equals("websocket")) {
            WebSocket.room(user);
        }
    }

}