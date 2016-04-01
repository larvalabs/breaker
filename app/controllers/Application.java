package controllers;

import com.google.gson.JsonObject;
import com.larvalabs.redditchat.dataobj.JsonUserSearch;
import com.larvalabs.redditchat.util.Util;
import models.ChatRoom;
import models.ChatUser;
import models.ChatUserRoomJoin;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import play.*;
import play.libs.OAuth2;
import play.libs.WS;
import play.mvc.Scope;
import play.mvc.With;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;

@With(ForceSSL.class)
public class Application extends PreloadUserController {

    public static final String REDDIT_CLIENTID = Play.configuration.getProperty("oauth.reddit.clientid");
    public static final String REDDIT_SECRET = Play.configuration.getProperty("oauth.reddit.secret");
    public static final String REDDIT_CALLBACK = Play.configuration.getProperty("oauth.reddit.callbackurl");
    public static final String REDDIT_SCOPE = WS.encode("identity,mysubreddits");
    // https://ssl.reddit.com/api/v1/authorize.compact
    public static final String REDDIT_AUTHORIZATION_URL = "https://www.reddit.com/api/v1/authorize?client_id=" + REDDIT_CLIENTID + "&response_type=code" +
            "&state=iuknjvdihu&redirect_uri=" + REDDIT_CALLBACK + "&duration=permanent&scope=" + REDDIT_SCOPE;
    public static final String REDDIT_TOKEN_URL = "https://www.reddit.com/api/v1/access_token";

    public static final String OAUTH_API_DOMAIN = "https://oauth.reddit.com";
    public static final String ENDPOINT_ME = OAUTH_API_DOMAIN + "/api/v1/me";
    public static final String COOKIE_CHATUSERNAME = "chatusername";
    public static final String COOKIE_CHATTOKEN = "chattoken";

    public static final String SESSION_JOINROOM = "joinroomname";
    public static final String SESSION_WAITROOM = "waitroomname";
    public static final String HTTPS_WWW_BREAKERAPP_COM = "https://www.breakerapp.com";

    public static void testForceLogin() {
        setUserInSession(ChatUser.findByUsername("chattest1"));
        test();
    }

    public static void fakeOtherUser() throws ChatUser.UserBannedException {
        ChatUser user = new ChatUser(Util.getUUID());
        user.setUsername("chattest2");
        user.save();
        user.joinChatRoom(ChatRoom.findByName("breakerapp"));

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
        try {
            chatUser.joinChatRoom(chatRoom);
        } catch (ChatUser.UserBannedException e) {
            index();
            return;
        }
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
                    try {
                        user.joinChatRoom(room);
                    } catch (ChatUser.UserBannedException e) {
                        Logger.error(e, "User banned, can't wait to open.");
                    }
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
            // NOTE: These soecific redirect calls are because the iframe https requirements are causing problems
            // with whatever play does for normal redirects
            if (joiningRoom != null) {
                session.remove(SESSION_JOINROOM);
                if (Play.mode.isProd()) {
                    redirect(HTTPS_WWW_BREAKERAPP_COM + "/c/" + joiningRoom);
                } else {
                    WebSocket.room(joiningRoom);
                }
            } else if (waitingRoom != null) {
                session.remove(SESSION_WAITROOM);
                if (Play.mode.isProd()) {
                    redirect(HTTPS_WWW_BREAKERAPP_COM + "/openroom/" + waitingRoom + "?accept=true");
                } else {
                    roomWait(waitingRoom, true);
                }
            } else {
                if (Play.mode.isProd()) {
                    redirect(HTTPS_WWW_BREAKERAPP_COM + "/c");
                } else {
                    WebSocket.room(null);
                }
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

    public static void markMessagesSeen(String roomName) {
        ChatUser user = connected();
        if (user == null) {
            Logger.debug("Can't mark room read, user isn't logged in.");
            error();
            return;
        }

        ChatRoom room = ChatRoom.findByName(roomName);
        if (room == null) {
            Logger.debug("Can't mark room read, room name " + roomName + " not found.");
            error();
            return;
        }
        ChatUserRoomJoin join = ChatUserRoomJoin.findByUserAndRoom(user, room);
        join.setLastSeenMessageTime(System.currentTimeMillis());
        join.save();

        Logger.debug(user.username + " last read time for " + roomName + " now " + join.getLastSeenMessageTime());
        ok();
    }

    public static void redditButton(String roomName, String imageFilename) throws IOException, FontFormatException {
        Logger.info("Generating sidebar button for room " + roomName);
        ChatRoom room = ChatRoom.findByName(roomName);
        if (room == null) {
            renderText("Not found.");
            return;
        }

        // Create image
        int width = 260, height = 45;
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        BufferedImage backgroundImage = ImageIO.read(new File("public/images/sidebar-buttonback.png"));

        // Get drawing context
        Font sourceSans = Font.createFont(Font.TRUETYPE_FONT, new File("fonts/SourceSansPro-Regular.otf"));
        Font normalFont = sourceSans.deriveFont(14f);
        Font sourceSansBold = Font.createFont(Font.TRUETYPE_FONT, new File("fonts/SourceSansPro-Bold.otf"));
        Font bigFont = sourceSansBold.deriveFont(16f);
        Graphics2D g2d = image.createGraphics();
        RenderingHints rh = new RenderingHints(
                RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        rh.add(new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON));
        rh.add(new RenderingHints(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON));
        g2d.setRenderingHints(rh);

        g2d.drawImage(backgroundImage, 0, 0, null);
        g2d.setColor(Color.WHITE);
        g2d.setFont(bigFont);
        g2d.drawString("/r/"+room.getName()+" chat", 70, 21);
        g2d.setFont(normalFont);
//        g2d.drawString(room.getNumberOfUsers() + " members", 70, 37);
        g2d.drawString("click to join", 70, 37);

        // Dispose context
        g2d.dispose();

        response.setContentTypeIfNotSet("image/png");
        ImageIO.write(image, "png", response.out);
    }
}