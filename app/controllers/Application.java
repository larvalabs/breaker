package controllers;

import com.google.gson.JsonObject;
import com.larvalabs.redditchat.Constants;
import com.larvalabs.redditchat.dataobj.*;
import com.larvalabs.redditchat.realtime.ChatRoomStream;
import com.larvalabs.redditchat.services.ActiveRoomsService;
import com.larvalabs.redditchat.util.RedditUtil;
import com.larvalabs.redditchat.util.RedisUtil;
import com.larvalabs.redditchat.util.Util;
import jobs.*;
import models.*;
import net.dean.jraw.ApiException;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import play.Logger;
import play.Play;
import play.db.jpa.JPA;
import play.libs.OAuth2;
import play.libs.WS;
import play.mvc.Scope;
import play.templates.JavaExtensions;
import reddit.RedditRequestError;

import javax.imageio.ImageIO;
import javax.persistence.Query;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;

public class Application extends PreloadUserController {

    public static final String REDDIT_CLIENTID = Play.configuration.getProperty("oauth.reddit.clientid");
    public static final String REDDIT_SECRET = Play.configuration.getProperty("oauth.reddit.secret");
    public static final String REDDIT_CALLBACK = Play.configuration.getProperty("oauth.reddit.callbackurl");
    public static final String REDDIT_SCOPE = WS.encode("identity,mysubreddits,flair");
    // https://ssl.reddit.com/api/v1/authorize.compact
    public static final String REDDIT_AUTHORIZATION_URL = "https://www.reddit.com/api/v1/authorize?client_id=" + REDDIT_CLIENTID + "&response_type=code" +
            "&state=iuknjvdihu&redirect_uri=" + REDDIT_CALLBACK + "&duration=permanent&scope=" + REDDIT_SCOPE;
    public static final String REDDIT_AUTHORIZATION_URL_COMPACT = "https://www.reddit.com/api/v1/authorize.compact?client_id=" + REDDIT_CLIENTID + "&response_type=code" +
            "&state=iuknjvdihu&redirect_uri=" + REDDIT_CALLBACK + "&duration=permanent&scope=" + REDDIT_SCOPE;
    public static final String REDDIT_TOKEN_URL = "https://www.reddit.com/api/v1/access_token";

    public static final String OAUTH_API_DOMAIN = "https://oauth.reddit.com";
    public static final String ENDPOINT_ME = OAUTH_API_DOMAIN + "/api/v1/me";
    public static final String COOKIE_CHATUSERNAME = "chatusername";
    public static final String COOKIE_CHATTOKEN = "chattoken";

    public static final String SESSION_JOINROOM = "joinroomname";
    public static final String SESSION_WAITROOM = "waitroomname";
    public static final String HTTPS_WWW_BREAKERAPP_COM = "https://www.breakerapp.com";

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

    public static void react() {
        ChatUser chatUser = connected();
        if (chatUser != null && chatUser.isAdmin()) {
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

    public static void create(String roomName) throws ChatUser.NoAccessToPrivateRoomException, ChatUser.UnableToCheckAccessToPrivateRoom, RedditRequestError, ChatRoom.SubredditDoesNotExistException {
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
        response.removeCookie(REMEMBERME_COOKIE);
        Logger.info("Removing rememberme cookies.");
        render();
    }

    public static void preAuthForRoomJoin(String roomName) {
        if (roomName != null) {
            // This is generally a direct link from a subreddit, so introduce things a bit
            ChatUser chatUser = connected();
            session.put(SESSION_JOINROOM, roomName);
            ChatRoom room = ChatRoom.findByName(roomName);
            String usernamesPresentStr = null;
            int userCount = 0;
            if (room != null) {
                TreeSet<String> usernamesPresent = room.getUsernamesPresent();
                if (usernamesPresent != null && usernamesPresent.size() > 0) {
                    userCount = usernamesPresent.size();
                    usernamesPresentStr = "";
                    for (String username : usernamesPresent) {
                        usernamesPresentStr += username + ", ";
                    }
                    usernamesPresentStr = usernamesPresentStr.substring(0, usernamesPresentStr.length() - 2);
                }
            }
            render(chatUser, roomName, usernamesPresentStr, userCount);
        } else {
            // This is probably a generic signup request from the homepage
            auth(null);
        }
    }

    public static void joinRoomCheck(String roomName) {
        if (roomName != null) {
            // This is generally a direct link from a subreddit, so introduce things a bit
            ChatUser chatUser = connected();
            session.put(SESSION_JOINROOM, roomName);
            ChatRoom room = ChatRoom.findByName(roomName);
            String roomInfoStr = null;
            int userCount = 0;
            if (room != null) {

                if (!room.isOpen()) {
                    roomWait(roomName, null);
                    return;
                }

                TreeSet<String> usernamesPresent = room.getUsernamesPresent();
                if (usernamesPresent != null && usernamesPresent.size() > 0) {
                    userCount = usernamesPresent.size();
                    String userPlural = "user" + JavaExtensions.pluralize(userCount);
                    String arePlural = JavaExtensions.pluralize(userCount, new String[]{"is", "are"});
                    roomInfoStr = "There " + arePlural + " " + userCount + " " + userPlural + " online now.";
                } else {
                    roomInfoStr = "This room currently has " + room.getUsers().size() + " members.";
                }
            }
            render(chatUser, roomName, roomInfoStr, userCount);
        } else {
            // This is probably a generic signup request from the homepage
            auth(null);
        }
    }

    public static void joinRoom(String roomName) {
        ChatUser user = connected();
        ChatRoom room = null;
        try {
            room = ChatRoom.findOrCreateForName(roomName);
        } catch (ChatRoom.SubredditDoesNotExistException e) {
            render("Application/subDoesNotExist.html");
            return;
        } catch (RedditRequestError redditRequestError) {
            render("Application/redditError.html");
            return;
        }

        if (!room.isOpen()) {
            roomWait(roomName, null);
            return;
        }

        try {
            user.joinChatRoom(room);
        } catch (ChatUser.UserBannedException e) {
            // todo show message that they're banned
            Application.index();
        } catch (ChatUser.UnableToCheckAccessToPrivateRoom unableToCheckAccessToPrivateRoom) {
            String errorMessage = "We are having a temporary problem verifying your access to this room, please try again later. (Usually this is a temporary problem contacting Reddit).";
            render("WebSocket/privateRoomError.html", room, errorMessage);
            return;
        } catch (ChatUser.NoAccessToPrivateRoomException e) {
            String errorMessage = "You do not have permission to access this room.";
            render("WebSocket/privateRoomError.html", room, errorMessage);
            return;
        }

        WebSocket.room(roomName);
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
        ChatRoom room = null;
        try {
            room = ChatRoom.findOrCreateForName(roomName);
        } catch (ChatRoom.SubredditDoesNotExistException e) {
            render("Application/subDoesNotExist.html");
            return;
        } catch (RedditRequestError redditRequestError) {
            render("Application/redditError.html");
            return;
        }

        if (accepting) {
            if (user == null) {
                // this is a logged out user trying to wait for this room, go to auth
                Logger.debug("User not logged in, redirecting to auth.");
                session.put(SESSION_WAITROOM, roomName);
                auth(null);
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
                    } catch (ChatUser.UnableToCheckAccessToPrivateRoom unableToCheckAccessToPrivateRoom) {
                        unableToCheckAccessToPrivateRoom.printStackTrace();
                    } catch (ChatUser.NoAccessToPrivateRoomException e) {
                        e.printStackTrace();
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

    public static void startAuthForGuest(String roomName, Boolean compact) {
        session.put(SESSION_JOINROOM, roomName);
        auth(compact);
    }

    public static void auth(Boolean compact) {
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
                if (Play.mode.isDev()) {
                    Logger.info("Access token: " + tokens.access);
                    Logger.info("Refresh token: " + tokens.refresh);
                }

                user.updateUserFromRedditJson(me);
                user.save();

                if (user.getChatRoomJoins() == null || user.getChatRoomJoins().size() == 0) {
                    ChatRoom defaultChat = ChatRoom.findByName(Constants.CHATROOM_DEFAULT);
                    try {
                        user.joinChatRoom(defaultChat);
                    } catch (ChatUser.UserBannedException | ChatUser.NoAccessToPrivateRoomException | ChatUser.UnableToCheckAccessToPrivateRoom e) {
                        Logger.error(e, "Unable to join default chat room for user " + username);
                    }
                }

                new UpdateUserFromRedditJob(user.getId()).afterRequest();

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
        if (compact != null) {
            redirect(REDDIT_AUTHORIZATION_URL_COMPACT);
        } else {
            redirect(REDDIT_AUTHORIZATION_URL);
        }
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
        headers.put("Authorization", "Basic " + Base64.encodeBase64String((REDDIT_CLIENTID + ":" + REDDIT_SECRET).getBytes()));

        WS.HttpResponse response = WS.url(REDDIT_TOKEN_URL).headers(headers).params(params).post();
        JsonObject jsonObject = response.getJson().getAsJsonObject();
        if (Play.mode.isDev()) {
            Logger.debug("JSON resp: " + jsonObject.toString());
            Logger.debug("JSON resp: " + jsonObject.get("access_token").toString());
        }
        return new Tokens(jsonObject.get("access_token").getAsString(), jsonObject.get("refresh_token").getAsString());
//        return jsonObject.get("accessToken").getAsString();
//        Logger.debug("Response: " + response);
//        return new OAuth2.Response(response);
    }

    private static HashMap<String, String> getOauthHeaders(String token) {
        HashMap<String, String> headers = new HashMap<String, String>();
        headers.put("User-Agent", "breakerapp/0.1 by megamatt2000");
        headers.put("Authorization", "bearer " + WS.encode(token));
        return headers;
    }

    /**
     * @deprecated Done on client side now.
     */
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

    public static void leaveRoom(String roomName) throws RedditRequestError, ChatRoom.SubredditDoesNotExistException {
        ChatUser user = connected();
        ChatRoom chatRoom = ChatRoom.findOrCreateForName(roomName);
        user.leaveChatRoom(chatRoom);
        ChatRoomStream.getEventStream(roomName).roomLeave(JsonChatRoom.from(chatRoom),
                JsonUser.fromUser(user, RedisUtil.isUserOnlineInAnyRoom(user.getUsername())));
        renderText("ok");
    }

    public static void initialState(Boolean test) {
        ChatUser user = connected();
        if (test != null && test) {
            user = ChatUser.findByUsername(Constants.USERNAME_REACTNATIVE_TESTUSER);
        }
        JsonUtil.FullState fullState = JsonUtil.loadFullStateForUser(user);
        renderJSON(fullState);
    }

    public static void optOut(String uuid, String username) {
        if (connected() != null) {
            Logger.info("User is logged in, redirect to prefs.");
            UserManage.prefs();
            return;
        }

        Message message = Message.findByUUID(uuid);
        if (message != null) {
            List<ChatUser> mentionedUsers = message.getMentionedUsers();
            for (ChatUser mentionedUser : mentionedUsers) {
                if (mentionedUser.getUsername().equalsIgnoreCase(username)) {
                    OptOutUser.addOptOut(mentionedUser.getUsername());
                    break;
                }
            }
            renderText("You are now opted out of all future messages.");
        } else {
            renderText("Invalid.");
        }
    }

    public static void getMessages(String roomName, Long id, Integer limit, Boolean before) {
        ChatUser connected = connected();
        if (connected == null) {
            error();
        }
        ChatRoom room = ChatRoom.findByName(roomName);
        if (before == null) {
            before = true;
        }
        if (limit == null || limit > 100) {
            limit = 20;
        }
        List<Message> messages;
        if (before) {
            messages = room.getMessagesWithoutBannedBefore(connected, id, limit);
        } else {
            messages = room.getMessagesWithoutBannedAfter(connected, id, limit);
        }
        List<JsonMessage> jsonMessages = new ArrayList<>();
        for (Message message : messages) {
            jsonMessages.add(JsonMessage.from(message, message.getUser().getUsername(), room.getName()));
        }
        renderJSON(jsonMessages);
    }

    public static void getActiveRooms(Integer limit, Integer offset) {
        ChatUser connected = connected();

        if(limit == null || limit > 25) limit = 10; //load max 25 Rooms
        if(offset == null) offset = 0;

        List<JsonActiveChatRoom> activeRooms = ActiveRoomsService.getActiveRooms(limit);
        HashMap<String, JsonActiveChatRoom> activeRoomMap = new HashMap<>();
        for (JsonActiveChatRoom activeRoom : activeRooms) {
            activeRoomMap.put(activeRoom.getName(), activeRoom);
        }
        renderJSON(activeRoomMap);
    }

    /*
        ADMIN FUNCTIONS
     */

    public static void adminOpenRoom(String roomName) {
        ChatUser user = connected();
        if (user.isAdmin()) {
            ChatRoom room = ChatRoom.findByName(roomName);
            room.setOpen(true);
            room.setNumNeededToOpen(0);
            room.save();
            WebSocket.room(roomName);
        } else {
            error();
        }
    }

    public static void testPM() throws ApiException {
        if (connected().isAdmin()) {
            RedditUtil.sendPrivateMessageFromBot("megamatt2000", "Server test PM", "Testing as of time " + System.currentTimeMillis());
            renderText("ok");
        } else {
            error("User is not an admin.");
        }
    }

    public static void runRedditUpdate(Long userId) {
        if (connected().isAdmin()) {
            if (userId != null) {
                new UpdateUserFromRedditJob(userId).now();
            } else {
                new UpdateAllUsersFromRedditRecurringJob().now();
            }
            renderText("OK");
        } else {
            error("User is not an admin.");
        }
    }

    public static void testQuery() {
        if (connected().isAdmin()) {
            Logger.info("Starting query...");
            Query getAllStuffQuery = JPA.em().createQuery("select ur from ChatUserRoomJoin ur join fetch ur.room urr join fetch ur.user u where ur.room in (select room from ChatUserRoomJoin ur2 where ur2.user = ?)")
                    .setParameter(1, ChatUser.findByUsername("megamatt2000"));
            List<ChatUserRoomJoin> resultList = getAllStuffQuery.getResultList();
            Logger.info("Iterating results...");
            for (ChatUserRoomJoin roomJoin : resultList) {
                Logger.info("Result: " + roomJoin.getRoom().getName() + " : " + roomJoin.getUser().getUsername());
            }
            Logger.info("Done.");
        } else {
            error("User is not an admin.");
        }
    }

    public static void testLinkbot(String subname) {
        if (connected().isAdmin()) {
            new RedditLinkBotJob(subname).now();
            renderText("ok");
        } else {
            error("User is not an admin.");
        }
    }

    public static void runRoomCheckJob() {
        if (connected().isAdmin()) {
            new MarkIncorrectRoomsDeletedJob().now();
            renderText("ok");
        } else {
            error("User is not an admin.");
        }
    }

    public static void makeMod(String roomname, String username, boolean mod) {
        if (connected().isAdmin()) {
            ChatRoom room = ChatRoom.findByName(roomname);
            ChatUser user = ChatUser.findByUsername(username);
            if (mod) {
                user.moderateRoom(room);
            } else {
                user.getModeratedRooms().remove(room);
            }
            user.save();
            room.save();
            room.refresh();
            ChatRoomStream.getEventStream(room.getName()).sendUserUpdate(room, user, true);
            ChatRoomStream.getEventStream(room.getName()).sendRoomUpdate(room);
            renderText("OK");
        } else {
            error("User is not an admin.");
        }
    }

    public static void runNewMessagesNotifications(long userId) {
        if (connected().isAdmin()) {
            new NotifyNewMessagesJob(userId).now();
            renderText("OK");
        } else {
            error("User is not an admin.");
        }
    }

    public static void runAllNewMessagesNotifications() {
        if (connected().isAdmin()) {
            new NotifyNewMessagesRecurringJob().now();
            renderText("OK");
        } else {
            error("User is not an admin.");
        }
    }

    public static void addOptOut(String username) {
        if (connected().isAdmin()) {
            OptOutUser optOutUser = new OptOutUser(username);
            optOutUser.save();
            renderText("OK");
        } else {
            error("User is not an admin.");
        }

    }

    public static void runDailyCleanup() {
        if (connected().isAdmin()) {
            new DailyCleanupJob().now();
            renderText("OK");
        } else {
            error("User is not an admin.");
        }

    }

    /**
     * Test methods
     */

    public static void testForceLogin() {
        if (Play.mode.isDev()) {
            setUserInSession(ChatUser.findByUsername("chattest1"));
            test();
        }
    }

    public static void fakeOtherUser() throws ChatUser.UserBannedException {
        if (Play.mode.isDev()) {
            ChatUser user = new ChatUser(Util.getUUID());
            user.setUsername("chattest-" + System.currentTimeMillis());
            user.save();
            try {
                user.joinChatRoom(ChatRoom.findByName(Constants.CHATROOM_DEFAULT));
            } catch (ChatUser.NoAccessToPrivateRoomException e) {
                e.printStackTrace();
            } catch (ChatUser.UnableToCheckAccessToPrivateRoom unableToCheckAccessToPrivateRoom) {
                unableToCheckAccessToPrivateRoom.printStackTrace();
            }
            WebSocket.room("breakerapp");
        }
    }
}
