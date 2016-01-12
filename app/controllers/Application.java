package controllers;

import com.google.gson.JsonObject;
import com.larvalabs.redditchat.dataobj.JsonUserSearch;
import models.ChatRoom;
import models.ChatUser;
import models.ChatUserRoomJoin;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import play.*;
import play.libs.OAuth2;
import play.libs.WS;
import play.mvc.*;

import java.util.*;

public class Application extends PreloadUserController {

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

    public static final String SESSION_JOINROOM = "joinroomname";
    public static final String SESSION_WAITROOM = "waitroomname";

    public static void testForceLogin() {
        setUserInSession(ChatUser.findByUsername("chattest1"));
        test();
    }


    public static void index() {
        ChatUser chatUser = connected();
        if (chatUser != null) {
            // go to chat
            WebSocket.room(null);
        } else {
            // static landing
            redirect("/");
        }
    }

    public static void test() {
        ChatUser chatUser = connected();
        render();
    }

    public static void create(String roomName) {
        ChatUser chatUser = connected();
        if (chatUser == null || chatUser.accessToken == null) {
            index();
            return;
        }

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

    public static void roomWait(String roomName, Boolean accept) {
        if (roomName == null) {
            index();
            return;
        }

        boolean accepting = false;
        if (accept != null && accept) {
            accepting = true;
        }

        ChatUser user = connected();
        ChatRoom room = ChatRoom.findOrCreateForName(roomName);

        if (accepting) {
            if (user == null) {
                // this is a logged out user trying to wait for this room, go to auth
                Logger.debug("User not logged in, redirecting to auth.");
                session.put(SESSION_WAITROOM, roomName);
                auth();
                return;
            } else {
                // this is a logged in user trying to wait for this room, wait it up
                if (ChatUserRoomJoin.findByUserAndRoom(user, room) == null) {
                    Logger.debug("User not already waiting for this room, reducing count needed.");
                    room.setNumNeededToOpen(room.getNumNeededToOpen() - 1);
                    if (room.getNumNeededToOpen() == 0) {
                        room.setOpen(true);
                    }
                    room.save();
                    user.joinChatRoom(room);
                }

                roomWait(roomName, null);
            }
        } else {
            if (user != null) {
                if (ChatUserRoomJoin.findByUserAndRoom(user, room) != null) {
                    Logger.debug("User already waiting for this room, show accept.");
                    render("Application/roomWaitAccept.html", user, room);
                    return;
                } else {
                    render(user, room);
                    return;
                }
            } else {
                render(user, room);
                return;
            }
        }


    }

/*
    public static void roomWaitAccept(String roomName) {
        if (roomName == null) {
            index();
            return;
        }

        if (user == null) {
            session.put(SESSION_WAITROOM, roomName);
            auth();
        } else {
            ChatRoom room = ChatRoom.findByName(roomName);
            if (ChatUserRoomJoin.findByUserAndRoom(user, room) == null) {
                Logger.debug("User not already waiting for this room, reducing count needed.");
                room.setNumNeededToOpen(room.getNumNeededToOpen() - 1);
                if (room.getNumNeededToOpen() == 0) {
                    room.setOpen(true);
                }
                room.save();
                user.joinChatRoom(room);
            }


            if (room.getNumNeededToOpen() == 0) {
                // todo make template
                render("Application/roomWaitOpen.html", user, room);
            } else {
                render(user, room);
            }
        }
    }
    */

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
            String waitingRoom = session.get(SESSION_WAITROOM);
            if (joiningRoom != null) {
                session.remove(SESSION_JOINROOM);
                WebSocket.room(joiningRoom);
            } else if (waitingRoom != null) {
                session.remove(SESSION_WAITROOM);
                roomWait(waitingRoom, true);
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

    public static void userSearch(String roomName, String query) {
        Logger.info("Searching in room " + roomName + " for user " + query);
        ChatRoom room = ChatRoom.findByName(roomName);
        if (room == null) {
            renderText("Not found.");
            return;
        }

        if (StringUtils.isEmpty(query)) {
            renderText("Query empty.");
            return;
        }

        // todo this will eventually be too slow, but for now fuuuuuuugettiboutit
        List<ChatUser> users = room.getUsers();
        TreeSet<String> usernamesPresent = room.getUsernamesPresent();
        JsonUserSearch userSearch = JsonUserSearch.make(roomName, query, users, usernamesPresent);
//        Logger.debug("Present  count: " + userSearch.onlineUsers.length);
//        Logger.debug("Online user count: " + userSearch.onlineUsers.length);
//        Logger.debug("Offline user count: " + userSearch.offlineUsers.length);
        renderJSON(userSearch);
    }
}