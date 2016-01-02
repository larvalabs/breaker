package controllers;

import com.google.gson.JsonObject;
import models.ChatRoom;
import models.ChatUser;
import org.apache.commons.codec.binary.Base64;
import play.*;
import play.libs.OAuth2;
import play.libs.WS;
import play.mvc.*;
import play.data.validation.*;

import java.util.*;

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

    public static final String SESSION_UID = "uid";
    public static final String SESSION_JOINROOM = "joinroomname";

    @Before
    static void preloadUser() {
        ChatUser user = null;
//        session.put(SESSION_UID, "2hfc8agp4k9ane");
        if (session.contains("uid")) {
            String uid = session.get(SESSION_UID);
            Logger.info("existing user: " + uid);
            user = ChatUser.get(uid);
            if (user != null) {
                renderArgs.put("user", user);
            }
        }
        // Only create user on successful auth
/*
        if (user == null) {
            user = ChatUser.createNew();
            session.put("uid", user.uid);
        }
*/
    }

    public static void testForceLogin() {
        setUserInSession(ChatUser.findByUsername("chattest1"));
        index();
    }

    private static void setUserInSession(ChatUser user) {
        session.put(SESSION_UID, user.uid);
        renderArgs.put("user", user);
    }

    private static ChatUser connected() {
        return (ChatUser) renderArgs.get("user");
    }

    public static void index() {
        ChatUser chatUser = connected();
        render();
    }

    public static void join(String roomName) {
        ChatUser chatUser = connected();
        if (chatUser == null || chatUser.accessToken == null) {
            preAuthForRoomJoin(roomName);
            return;
        }

        session.remove(SESSION_JOINROOM);
        ChatRoom chatRoom = ChatRoom.findOrCreateForName(roomName);
        chatUser.joinChatRoom(chatRoom);
        WebSocket.room(roomName);
    }

    public static void logout() {
        session.remove(SESSION_UID);
        redirect("/");
//        index();
    }

    public static void preAuthForRoomJoin(String roomName) {
        if (roomName != null) {
            // This is generally a direct link from a subreddit, so introduce things a bit
            ChatUser chatUser = connected();
            session.put(SESSION_JOINROOM, roomName);
            render(chatUser, roomName);
        } else {
            // This is probably a generic signup request from the homepage
            auth();
        }
    }

    public static void auth() {
        Logger.debug("Received auth response.");
        if (OAuth2.isCodeResponse()) {
//            ChatUser user = connected();
//            OAuth2.Response response = retrieveAccessToken(REDDIT_CALLBACK);
            Tokens tokens = retrieveAccessToken(REDDIT_CALLBACK);

            if (tokens.access != null) {

//            Http.Header[] headers = new Http.Header[2];
                HashMap<String, String> headers = getOauthHeaders(tokens.access);
                JsonObject me = WS.url(ENDPOINT_ME)
                        .headers(headers)
                        .get().getJson().getAsJsonObject();

                String username = me.get("name").getAsString();
                Logger.info("Trying to log in " + username);
                ChatUser user = ChatUser.findOrCreate(username);
                user.username = username;
                user.accessToken = tokens.access;
                user.refreshToken = tokens.refresh;

                user.linkKarma = me.get("link_karma").getAsLong();
                user.commentKarma = me.get("comment_karma").getAsLong();
                user.lastResponseApiMe = me.toString();
                user.save();

                setUserInSession(user);
            }

            String joiningRoom = session.get(SESSION_JOINROOM);
            if (joiningRoom != null) {
                join(joiningRoom);
            } else {
                WebSocket.room(null);
            }
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

}