package models;

import com.larvalabs.redditchat.Constants;
import com.larvalabs.redditchat.dataobj.JsonUser;
import com.sun.istack.internal.Nullable;
import jobs.SaveLastReadTimeForAllPendingJob;
import play.Logger;
import play.db.DB;
import play.db.jpa.Model;
import play.modules.redis.Redis;
import reddit.BreakerRedditClient;
import reddit.ResourceNotFoundException;

import javax.persistence.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

@Entity
@Table(name = "chatroom")
public class ChatRoom extends Model {

    public static final double CHANCE_CLEAN_REDIS_PRESENCE = 0.1;
    private static Random random = new Random();

    public static final String SUBREDDIT_ANDROID = "android";

    public static final int ICON_SOURCE_NONE = 0;
    public static final int ICON_SOURCE_PLAY_STORE = 1;
    public static final int ICON_SOURCE_HIAPK = 2;
    public static final String REDISKEY_PRESENCE_GLOBAL = "presence__global";

    @Column(unique = true)
    public String name;

    public int iconUrlSource = ICON_SOURCE_NONE;
    public boolean noIconAvailableFromStore = false;
    public Date iconRetrieveDate;

    // A denormalized count of number of users in chat room
    public long numberOfUsers;

    public boolean needsScoreRecalc;

    public boolean open = true;
    public int numNeededToOpen = Constants.NUM_PEOPLE_TO_OPEN_ROOM;

    @ManyToMany(mappedBy = "watchedRooms", fetch = FetchType.LAZY)
    public Set<ChatUser> watchers = new HashSet<ChatUser>();

    @ManyToMany(cascade = CascadeType.PERSIST)
    @JoinTable(name = "user_bannedroom")
    public Set<ChatUser> bannedUsers = new HashSet<ChatUser>();

    // Moderator stuff
    @ManyToMany(mappedBy = "moderatedRooms", fetch = FetchType.LAZY)
    public Set<ChatUser> moderators = new HashSet<ChatUser>();

    public String iconUrl;
    public String banner;
    public String flairScale;

    public int karmaThreshold = Constants.DEFAULT_MIN_KARMA_REQUIRED_TO_POST;
    public int sidebarColor;

    public String sidebarBackgroundColor;
    public String sidebarTextColor;
    public String sidebarRoomSelectedColor;
    public String sidebarRoomHoverColor;
    public String sidebarRoomTextColor;
    public String sidebarUnreadColor;
    public String sidebarUnreadTextColor;
    public String signinButtonColor;
    public String signinButtonTextColor;

    public boolean privateRoom;

    public ChatRoom(String name) {
        this.name = name;
        this.numberOfUsers = 0;
    }

    public String getFlairScale(){
        return flairScale;
    }

    public void setFlairScale(String flairScale){
        this.flairScale = flairScale;
    }

    public String getName() {
        return name;
    }

    public String getSidebarBackgroundColor(){
        return sidebarBackgroundColor;
    }

    public void setSidebarBackgroundColor(String sidebarBackgroundColor){
        this.sidebarBackgroundColor = sidebarBackgroundColor;
    }

    public String getSidebarTextColor(){
        return sidebarTextColor;
    }

    public void setSidebarTextColor(String sidebarTextColor){
        this.sidebarTextColor = sidebarTextColor;
    }

    public String getSigninButtonTextColor(){
        return signinButtonTextColor;
    }

    public String getSidebarRoomSelectedColor(){
        return sidebarRoomSelectedColor;
    }

    public String getSidebarRoomHoverColor(){
        return sidebarRoomHoverColor;
    }

    public String getSidebarRoomTextColor(){
        return sidebarRoomTextColor;
    }

    public String getSidebarUnreadColor(){
        return sidebarUnreadColor;
    }

    public String getSidebarUnreadTextColor(){
        return sidebarUnreadTextColor;
    }

    public String getSigninButtonColor(){
        return signinButtonColor;
    }

    public void setSidebarRoomSelectedColor(String sidebarRoomSelectedColor){
        this.sidebarRoomSelectedColor = sidebarRoomSelectedColor;
    }

    public void setSidebarRoomHoverColor(String sidebarRoomHoverColor){
        this.sidebarRoomHoverColor = sidebarRoomHoverColor;
    }

    public void setSidebarRoomTextColor(String sidebarRoomTextColor){
        this.sidebarRoomTextColor = sidebarRoomTextColor;
    }

    public void setSidebarUnreadColor(String sidebarUnreadColor){
        this.sidebarUnreadColor = sidebarUnreadColor;
    }

    public void setSidebarUnreadTextColor(String sidebarUnreadTextColor){
        this.sidebarUnreadTextColor = sidebarUnreadTextColor;
    }

    public void setSigninButtonColor(String signinButtonColor){
        this.signinButtonColor = signinButtonColor;
    }

    public void setSigninButtonTextColor(String signinButtonTextColor){
        this.signinButtonTextColor = signinButtonTextColor;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public String getIconUrl(int size) {
        return iconUrl;
    }

    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }

    public int getIconUrlSource() {
        return iconUrlSource;
    }

    public void setIconUrlSource(int iconUrlSource) {
        this.iconUrlSource = iconUrlSource;
    }

    public Date getIconRetrieveDate() {
        return iconRetrieveDate;
    }

    public void setIconRetrieveDate(Date iconRetrieveDate) {
        this.iconRetrieveDate = iconRetrieveDate;
    }

    public boolean isNoIconAvailableFromStore() {
        return noIconAvailableFromStore;
    }

    public void setNoIconAvailableFromStore(boolean noIconAvailableFromStore) {
        this.noIconAvailableFromStore = noIconAvailableFromStore;
    }

    public String getBanner() {
        return banner;
    }

    public void setBanner(String banner) {
        this.banner = banner;
    }

    public int getKarmaThreshold() {
        return karmaThreshold;
    }

    public void setKarmaThreshold(int karmaThreshold) {
        this.karmaThreshold = karmaThreshold;
    }

    public int getSidebarColor() {
        return sidebarColor;
    }

    public void setSidebarColor(int sidebarColor) {
        this.sidebarColor = sidebarColor;
    }

    public long getNumberOfUsers() {
        return numberOfUsers;
    }

    public void setNumberOfUsers(long numberOfUsers) {
        this.numberOfUsers = numberOfUsers;
    }

    public boolean isNeedsScoreRecalc() {
        return needsScoreRecalc;
    }

    public void setNeedsScoreRecalc(boolean needsScoreRecalc) {
        this.needsScoreRecalc = needsScoreRecalc;
    }

    public void setNeedsScoreRecalcIfNecessaryAndSave(boolean needsScoreRecalc) {
        if (this.needsScoreRecalc != needsScoreRecalc) {
            this.needsScoreRecalc = needsScoreRecalc;
            save();
        }
    }

    public boolean isOpen() {
        return open;
    }

    public void setOpen(boolean open) {
        this.open = open;
    }

    public int getNumNeededToOpen() {
        return numNeededToOpen;
    }

    public void setNumNeededToOpen(int numNeededToOpen) {
        this.numNeededToOpen = numNeededToOpen;
    }

    public Set<ChatUser> getWatchers() {
        return watchers;
    }

    public void setWatchers(Set<ChatUser> watchers) {
        this.watchers = watchers;
    }

    public Set<ChatUser> getBannedUsers() {
        return bannedUsers;
    }

    public void setBannedUsers(Set<ChatUser> bannedUsers) {
        this.bannedUsers = bannedUsers;
    }

    public Set<ChatUser> getModerators() {
        return moderators;
    }

    public void setModerators(Set<ChatUser> moderators) {
        this.moderators = moderators;
    }

    public void addModerator(ChatUser chatUser) {
        chatUser.moderateRoom(this);
    }

    public List<String> getModeratorUsernames() {
        List<String> usernames = new ArrayList<>();
        for (ChatUser moderator : moderators) {
            usernames.add(moderator.getUsername());
        }
        return usernames;
    }

    public boolean isPrivateRoom() {
        return privateRoom;
    }

    public void setPrivateRoom(boolean privateRoom) {
        this.privateRoom = privateRoom;
    }

    // Do stuff zone

    public static ChatRoom findByName(String name) {
        if (name == null) {
            return null;
        }
        name = name.toLowerCase().trim();
        return find("LOWER(name)", name).first();
    }

    private static final String BASE_MSG_QUERY = "room = ? and deleted = false and flagCount < "+ Constants.THRESHOLD_MESSAGE_FLAG
            +" and user.flagCount < " + Constants.USER_FLAG_THRESHOLD + " and (user.deviceShadowBan = false or user = ?)";

    public List<Message> getTopMessagesWithoutBanned(ChatUser loggedInUser, int limit) {
        return Message.find(BASE_MSG_QUERY + " order by score desc", this, loggedInUser).fetch(limit);
    }

    public List<Message> getMessagesWithoutBanned(ChatUser loggedInUser, int limit) {
        return Message.find(BASE_MSG_QUERY + " order by id desc", this,loggedInUser).fetch(limit);
    }

    public List<Message> getMessagesWithoutBanned(ChatUser loggedInUser, long afterMessageId, int limit) {
        List<Message> messages = Message.find(BASE_MSG_QUERY + " and id > ? order by id asc", this, loggedInUser, afterMessageId).fetch(limit);
        Collections.reverse(messages);
        return messages;
    }

    public List<Message> getMessagesWithoutBannedCenteredOn(ChatUser loggedInUser, long centerOnMessageId, int limit) {
        int afterAmount = 5;
        List<Message> itemAndAfterItems = Message.find(BASE_MSG_QUERY + " and id >= ? order by id asc", this, loggedInUser, centerOnMessageId).fetch(afterAmount);
        List<Message> beforeItems = Message.find(BASE_MSG_QUERY + " and id < ? order by id desc", this, loggedInUser, centerOnMessageId).fetch(limit - itemAndAfterItems.size());
        Collections.reverse(itemAndAfterItems);
        itemAndAfterItems.addAll(beforeItems);
        return itemAndAfterItems;
    }

    public boolean isModerator(ChatUser user) {
        return getModerators().contains(user) || user.isAdmin();
    }

    public boolean isRedditModerator(ChatUser user) {
        return getModerators().contains(user);
    }

    public List<Message> getMessagesWithoutBannedBefore(ChatUser loggedInUser, long beforeMessageId, int limit) {
        return Message.find(BASE_MSG_QUERY + " and id < ? order by id desc", this, loggedInUser, beforeMessageId).fetch(limit);
    }

    /**
     * Warning: Unfiltered - contains deleted and flagged, etc.
     * Should probably only be used for server stuff like rescoring
     * @param limit
     * @return
     */
    public List<Message> getMessages(int limit) {
        return Message.find("room = ? and deleted != true order by id desc", this).fetch(limit);
    }

    public List<Message> getMessagesByUser(ChatUser user, int limit) {
        return Message.find("user = ? and room = ? and deleted != true order by id desc", user, this).fetch(limit);
    }

    public List<Message> getMessages(long beforeMessageId, int limit) {
        return Message.find("id < ? and room = ? order by id desc", beforeMessageId, this).fetch(limit);
    }

    public static List<ChatRoom> getRoomsNeedingIconRetrieval(int limit) {
        return ChatRoom.find("iconUrl = null and noIconAvailableFromStore = false").fetch(limit);
    }

    public List<ChatUser> getUsers() {
        List<ChatUserRoomJoin> joins = ChatUserRoomJoin.findByChatRoom(this);
        List<ChatUser> users = new ArrayList<ChatUser>();
        for (ChatUserRoomJoin chatUserRoomJoin : joins) {
            users.add(chatUserRoomJoin.user);
        }
        Collections.sort(users, new Comparator<ChatUser>() {
            @Override
            public int compare(ChatUser o1, ChatUser o2) {
                return o1.getUsername().toLowerCase().compareTo(o2.getUsername().toLowerCase());
            }
        });
        return users;
    }

    public static ChatRoom findOrCreateForName(String name) {
        ChatRoom chatRoom = findByName(name);
        if (chatRoom == null) {
            Logger.info("Couldn't find chat room " + name + ", creating.");
            chatRoom = new ChatRoom(name);
            BreakerRedditClient client = new BreakerRedditClient();
            boolean isPrivate = false;
            try {
                isPrivate = client.isSubredditPrivate(name);
                Logger.info(name + " is a private room.");
            } catch (ResourceNotFoundException e) {
                Logger.warn("Subreddit " + name + " not found, maybe block creating this in the future?");
            }
            chatRoom.setPrivateRoom(isPrivate);
            chatRoom.save();

            // Auto join admins
            List<ChatUser> admins = ChatUser.findAdmins();
            for (ChatUser admin : admins) {
                try {
                    Logger.info("Adding admin user " + admin.getUsername() + " to new room " + name);
                    admin.joinChatRoom(chatRoom);
                } catch (ChatUser.UserBannedException | ChatUser.NoAccessToPrivateRoomException | ChatUser.UnableToCheckAccessToPrivateRoom e) {
                    e.printStackTrace();
                }
            }
        } else {
//            Logger.info("Found chat room for app " + packageName);
        }
        return chatRoom;
    }

    public void markMessagesSeen(ChatUser user) {
        markMessagesSeen(user, null);
    }

    public void markMessagesSeen(ChatUser user, Message lastMessage) {
        if (lastMessage != null) {
            Logger.info("Marking messages read in " + name);
            if (lastMessage == null) {
                lastMessage = getMessages(1).get(0);
            }
            if (lastMessage != null) {
                Logger.info("Marking messages read last message " + lastMessage.getId());
            } else {
                Logger.info("Can't mark read because no messages in room " + name);
                return;
            }
            ChatUserRoomJoin join = ChatUserRoomJoin.findByUserAndRoom(user, this);
            if (join != null) {
                join.setLastSeenMessageId(lastMessage.getId());
                join.save();
                Logger.info("Marking messages read successful, last read id should be " + lastMessage.getId());
            }
        }
    }

    private static String getRedisPresenceKeyForRoom(String roomName) {
        return "presence_" + roomName;
    }

    public long getCurrentUserCount() {
        return getCurrentUserCount(name);
    }

    public static long getCurrentUserCount(String roomName) {
        try {
            int time = (int) (System.currentTimeMillis() / 1000);
            Long zcount = Redis.zcount(getRedisPresenceKeyForRoom(roomName), time - Constants.PRESENCE_TIMEOUT_SEC, time);
//        Logger.info(appPackage + " live count " + zcount);
            return zcount;
        } catch (Exception e) {
            Logger.error("Error contacting redis.");
            return 0;
        }
    }

    /**
     * This is probably slow as hell, but ok for v1
     * @return
     */
    public TreeSet<ChatUser> getPresentUserObjects() {
        TreeSet<String> usernamesPresent = getUsernamesPresent();
        TreeSet<ChatUser> users = new TreeSet<ChatUser>(new Comparator<ChatUser>() {
            @Override
            public int compare(ChatUser o1, ChatUser o2) {
                return o1.username.compareTo(o2.username);
            }
        });
        for (String username : usernamesPresent) {
            ChatUser chatUser = ChatUser.findByUsername(username);
            if (chatUser != null) {
                users.add(chatUser);
            }
        }
        return users;
    }

    public JsonUser[] getPresentJsonUsers() {
        TreeSet<ChatUser> presentUserObjects = getPresentUserObjects();
        JsonUser[] users = new JsonUser[presentUserObjects.size()];
        int i = 0;
        for (ChatUser presentUserObject : presentUserObjects) {
            users[i] = JsonUser.fromUser(presentUserObject, true);
            i++;
        }
        return users;
    }

    public JsonUser[] getAllUsersWithOnlineStatus() {
//        TreeSet<ChatUser> presentUserObjects = getPresentUserObjects();
        HashSet<ChatUser> allUsers = new HashSet<ChatUser>(getUsers());

        JsonUser[] users = new JsonUser[allUsers.size()];
        TreeSet<String> usernamesPresent = getUsernamesPresent();
        int i = 0;
        for (ChatUser user : allUsers) {
            users[i] = JsonUser.fromUser(user, usernamesPresent.contains(user.getUsername()));
            i++;
        }
        return users;
    }

    public TreeSet<String> getUsernamesPresent() {
        return getUsernamesPresent(name);
    }

    // note: could consider doing this as a separate set of just usernames
    public static TreeSet<String> getUsernamesPresent(String roomName) {
        try {
            int time = (int) (System.currentTimeMillis() / 1000);
            Set<String> usersPresent = Redis.zrangeByScore(getRedisPresenceKeyForRoom(roomName), time - Constants.PRESENCE_TIMEOUT_SEC, time);
            TreeSet<String> usernamesPresent = new TreeSet<String>();
            for (String usernameAndConnStr : usersPresent) {
                usernamesPresent.add(splitUsernameAndConnection(usernameAndConnStr)[0]);
            }

/*
        usersPresent.add("test1");
        usersPresent.add("horribleidiot");
        usersPresent.add("giantmoron");
        usersPresent.add("bigloser");
        usersPresent.add("wingotango");
*/
            return usernamesPresent;
        } catch (Exception e) {
            Logger.error(e, "Error contacting redis.");
            return new TreeSet<String>();
        }
    }

    /**
     * Check if the username is present, loads the list of usernames present.
     * @param user
     * @return
     */
    public boolean isUserPresent(String username) {
        return isUserPresent(name, username);
    }

    /**
     * Check if the username is present, optionally loads the usernames present.
     * @param user
     * @param usernamesPresent if null, load usernames present from redis
     * @return
     */
    public boolean isUserPresent(ChatUser user, @Nullable Set<String> usernamesPresent) {
        if (usernamesPresent == null) {
            usernamesPresent = getUsernamesPresent();
        }
        return usernamesPresent.contains(user.username);
/*
        try {
            Double zscore = Redis.zscore(getRedisPresenceKeyForRoom(), user.username);
            return zscore != null;
        } catch (Exception e) {
            Logger.error("Error contacting redis.");
        }
        return false;
*/
    }

    public static boolean isUserPresent(String roomName, String username) {
        return getUsernamesPresent(roomName).contains(username);
    }

    public void userPresent(String username, String connectionId) {
        userPresent(name, username, connectionId);
    }

    public static void userPresent(String roomName, String username, String connectionId) {
        try {
            int time = (int) (System.currentTimeMillis() / 1000);
            Redis.zadd(getRedisPresenceKeyForRoom(roomName), time, getUsernameAndConnectionString(username, connectionId));
            if (random.nextFloat() < CHANCE_CLEAN_REDIS_PRESENCE) {
                // this is just housekeeping to keep the sets from getting too big
                cleanPresenceSet(roomName);
            }
            userPresentGlobal(username);
        } catch (Exception e) {
            Logger.error(e, "Error contacting redis.");
        }
    }

    private static String getUsernameAndConnectionString(String username, String connectionId) {
        return username + ":" + connectionId;
    }

    /**
     * Add to list of all online users across rooms
     * @param username
     */
    private static void userPresentGlobal(String username) {
        try {
            int time = (int) (System.currentTimeMillis() / 1000);
            Redis.zadd(REDISKEY_PRESENCE_GLOBAL, time, username);
            if (random.nextFloat() < CHANCE_CLEAN_REDIS_PRESENCE) {
                // this is just housekeeping to keep the sets from getting too big
                cleanPresenceSetGlobal();
            }
        } catch (Exception e) {
            Logger.error(e, "Error contacting redis.");
        }
    }

    private static void userNotPresentGlobal(String username) {
        try {
            Redis.zrem(REDISKEY_PRESENCE_GLOBAL, username);
        } catch (Exception e) {
            Logger.error(e, "Error contacting redis.");
        }
    }

    /**
     * Housekeeping to keep list sizes under control
     */
    private static void cleanPresenceSetGlobal() {
        try {
            int time = (int) (System.currentTimeMillis() / 1000);
            Long removed = Redis.zremrangeByScore(REDISKEY_PRESENCE_GLOBAL, 0, time - Constants.PRESENCE_TIMEOUT_SEC * 2);
//            Logger.debug("Clean of presence set for " + name + " removed " + removed + " elements.");
        } catch (Exception e) {
            Logger.error(e, "Error contacting redis.");
        }
    }

    /**
     * This might be better in another class
     * @return List of all online usernames, across all rooms.
     */
    public static TreeSet<String> getAllOnlineUsersForAllRooms() {
        try {
            int time = (int) (System.currentTimeMillis() / 1000);
            Set<String> usersPresent = Redis.zrangeByScore(REDISKEY_PRESENCE_GLOBAL, time - Constants.PRESENCE_TIMEOUT_SEC, time);
            TreeSet<String> usernamesPresent = new TreeSet<String>();
            for (String username : usersPresent) {
                usernamesPresent.add(username);
            }

            return usernamesPresent;
        } catch (Exception e) {
            Logger.error(e, "Error contacting redis.");
            return new TreeSet<String>();
        }
    }

    public static boolean isUserOnlineInAnyRoom(String username) {
        return getAllOnlineUsersForAllRooms().contains(username);
    }

    /**
     *
     * @param
     * @return [0] = username, [1] = connectionId
     */
    private static String[] splitUsernameAndConnection(String combined) {
        return combined.split(":");
    }

    public void userNotPresent(String username, String connectionId) {
        userNotPresent(name, username, connectionId);
    }

    public static void userNotPresent(String roomName, String username, String connectionId) {
        try {
            Redis.zrem(getRedisPresenceKeyForRoom(roomName), getUsernameAndConnectionString(username, connectionId));
            userNotPresentGlobal(username);
        } catch (Exception e) {
            Logger.error(e, "Error contacting redis.");
        }
    }

    private static void cleanPresenceSet(String roomName) {
        try {
            int time = (int) (System.currentTimeMillis() / 1000);
            Long removed = Redis.zremrangeByScore(getRedisPresenceKeyForRoom(roomName), 0, time - Constants.PRESENCE_TIMEOUT_SEC * 2);
//            Logger.debug("Clean of presence set for " + name + " removed " + removed + " elements.");
        } catch (Exception e) {
            Logger.error(e, "Error contacting redis.");
        }
    }

    private static String makeKeyForLastReadTime(String roomName, String username) {
        return "lastread-" + roomName + "-" + username;
    }

    public void markMessagesReadForUser(String username) {
        markMessagesReadForUser(name, username);
    }

    public static long markMessagesReadForUser(String roomName, String username) {
        long newLastReadTime = System.currentTimeMillis();
        try {
            Redis.set(makeKeyForLastReadTime(roomName, username), ""+ newLastReadTime);
            SaveLastReadTimeForAllPendingJob.addPendingUsername(username, roomName);
        } catch (Exception e) {
            Logger.error(e, "Error contacting redis.");
        }
        return newLastReadTime;
    }

    public long getLastMessageReadTimeForUser(String username) {
        return getLastMessageReadTimeForUser(name, username);
    }

    public static long getLastMessageReadTimeForUser(String roomName, String username) {
        try {
            String lastTime = Redis.get(makeKeyForLastReadTime(roomName, username));
            if (lastTime != null) {
                return Long.parseLong(lastTime);
            }
        } catch (Exception e) {
            Logger.error(e, "Error contacting redis.");
        }
        return 0;
    }

    public static List<ChatRoom> getTopRooms(boolean hardware, int days, int limit) {
        ArrayList<ChatRoom> topRooms = new ArrayList<ChatRoom>();
        Logger.info("Top rooms:");
        final ResultSet resultSet = DB.executeQuery("select max(r.id), sum(1) as count from message m, chatroom r where m.room_id=r.id and createDate >= ( NOW() - INTERVAL '" + days + " DAY' ) and hardware = " + hardware + " and numberOfUsers > " + Constants.THRESHOLD_ROOM_USERS_FOR_TOP_LIST + " group by room_id order by count desc limit " + limit);
        try {
            while (resultSet.next()) {
                Long roomId = resultSet.getLong(1);
                int messageCount = resultSet.getInt(2);
                ChatRoom room = ChatRoom.findById(roomId);
                if (room != null) {
                    Logger.info(room.getName() + " with " + messageCount + " messages");
                    topRooms.add(room);
                } else {
                    Logger.warn("Couldn't find room for id " + roomId);
                }
            }
        } catch (SQLException e) {
            Logger.error(e, "Problem getting top rooms.");
            return null;
        } finally {
            if (resultSet != null) {
                try {
                    resultSet.close();
                } catch (SQLException e) {
                    Logger.error(e, "Error getting unread counts.");
                }
            }
        }
        return topRooms;
    }

    public boolean userCanPost(ChatUser chatUser) {
        ChatUserRoomJoin roomJoin = ChatUserRoomJoin.findByUserAndRoom(chatUser, this);
        if (roomJoin == null) {
            return false;
        }
        if (getBannedUsers().contains(chatUser)) {
            Logger.debug("User " + chatUser.getUsername() + " is banned from " + name + " and cannot post.");
            return false;
        }
        if (chatUser.getLinkKarma() + chatUser.getCommentKarma() < getKarmaThreshold()) {
            Logger.debug("User is below karma threshold.");
            return false;
        }
        if (chatUser.isFlagBanned() || chatUser.isShadowBan()) {
            Logger.debug("User " + chatUser.getUsername() + " is flag or shadow banned.");
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "Room: " + getId() + ":" + getName();
    }

    public boolean isDefaultRoom() {
        return getName().equals(Constants.CHATROOM_DEFAULT);
    }
}
