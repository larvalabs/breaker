package models;

import com.larvalabs.redditchat.Constants;
import com.larvalabs.redditchat.dataobj.BreakerCache;
import com.larvalabs.redditchat.dataobj.JsonUser;
import com.larvalabs.redditchat.realtime.ChatRoomStream;
import com.larvalabs.redditchat.util.RedisUtil;
import com.sun.istack.internal.Nullable;
import jobs.SaveLastReadTimeForAllPendingJob;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import play.Logger;
import play.db.DB;
import play.db.jpa.JPABase;
import play.db.jpa.Model;
import play.modules.redis.Redis;
import reddit.BreakerRedditClient;
import reddit.RedditRequestError;
import reddit.ResourceNotFoundException;

import javax.persistence.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

@Entity
@Table(name = "chatroom")
public class ChatRoom extends Model {

    public static final String SUBREDDIT_ANDROID = "android";

    public static final int ICON_SOURCE_NONE = 0;
    public static final int ICON_SOURCE_PLAY_STORE = 1;
    public static final int ICON_SOURCE_HIAPK = 2;

    public static final String PREFVAL_LINKBOT_ALLNEW = "allnew";
    public static final String PREFVAL_LINKBOT_NEWTOP = "newtop";
    public static final String PREFVAL_LINKBOT_NONE = "none";

    @Column(unique = true)
    public String name;
    public String displayName;

    public int iconUrlSource = ICON_SOURCE_NONE;
    public boolean noIconAvailableFromStore = false;
    public Date iconRetrieveDate;

    // A denormalized count of number of users in chat room
    public long numberOfUsers;

    public boolean needsScoreRecalc;

    public boolean open = false;
    public int numNeededToOpen = Constants.NUM_PEOPLE_TO_OPEN_ROOM;

    @ManyToMany(mappedBy = "watchedRooms", fetch = FetchType.LAZY)
    public Set<ChatUser> watchers = new HashSet<ChatUser>();

    @ManyToMany(cascade = CascadeType.PERSIST)
    @JoinTable(name = "user_bannedroom")
    public Set<ChatUser> bannedUsers = new HashSet<ChatUser>();

    // Moderator stuff
    @ManyToMany(mappedBy = "moderatedRooms", fetch = FetchType.LAZY)
    public Set<ChatUser> moderators = new HashSet<ChatUser>();

    @ManyToMany(mappedBy = "moderatedRoomsChatOnly", fetch = FetchType.LAZY)
    public Set<ChatUser> moderatorsChatOnly = new HashSet<ChatUser>();

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

    public String linkBotPref = PREFVAL_LINKBOT_NEWTOP;

    public boolean deleted = false;

    public ChatRoom(String displayName) {
        this.name = displayName.toLowerCase();
        this.displayName = displayName;
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

    public void setName(String name) {
        this.name = name;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
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

    public Set<ChatUser> getModeratorsChatOnly() {
        return moderatorsChatOnly;
    }

    public void setModeratorsChatOnly(Set<ChatUser> moderatorsChatOnly) {
        this.moderatorsChatOnly = moderatorsChatOnly;
    }

    public void addModerator(ChatUser chatUser, boolean chatRoomType) {
        chatUser.moderateRoom(this, chatRoomType);
    }

    public List<String> getModeratorUsernames() {
        List<String> usernames = new ArrayList<>();
        for (ChatUser moderator : moderators) {
            usernames.add(moderator.getUsername());
        }
        for (ChatUser moderator : moderatorsChatOnly) {
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

    public String getLinkBotPref() {
        return linkBotPref;
    }

    public void setLinkBotPref(String linkBotPref) {
        this.linkBotPref = linkBotPref;
    }

    public boolean isLinkBotPrefAllNew() {
        return linkBotPref != null && linkBotPref.equals(PREFVAL_LINKBOT_ALLNEW);
    }

    public boolean isLinkBotPrefNewTop() {
        return linkBotPref != null && linkBotPref.equals(PREFVAL_LINKBOT_NEWTOP);
    }

    public boolean isLinkBotNone() {
        return linkBotPref != null && linkBotPref.equals(PREFVAL_LINKBOT_NONE);
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
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
            +" and user.flagCount < " + Constants.USER_FLAG_THRESHOLD + " and (user.shadowBan = false or user = ?)";

    public List<Message> getTopMessagesWithoutBanned(ChatUser loggedInUser, int limit) {
        return Message.find(BASE_MSG_QUERY + " order by score desc", this, loggedInUser).fetch(limit);
    }

    public List<Message> getMessagesWithoutBanned(ChatUser loggedInUser, int limit) {
        return Message.find(BASE_MSG_QUERY + " order by id desc", this,loggedInUser).fetch(limit);
    }

    public List<Message> getMessagesWithoutBannedBefore(ChatUser loggedInUser, long beforeMessageId, int limit) {
        return Message.find(BASE_MSG_QUERY + " and id < ? order by id desc", this, loggedInUser, beforeMessageId).fetch(limit);
    }

    public List<Message> getMessagesWithoutBannedAfter(ChatUser loggedInUser, long afterMessageId, int limit) {
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
        return getModerators().contains(user) || getModeratorsChatOnly().contains(user) || user.isAdmin();
    }

    public boolean isRedditModerator(ChatUser user) {
        return getModerators().contains(user);
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

    public static class SubredditDoesNotExistException extends Exception {
    }

    public static ChatRoom findOrCreateForName(String name) throws SubredditDoesNotExistException, RedditRequestError {
        ChatRoom chatRoom = findByName(name);
        if (chatRoom == null) {
            Logger.info("Couldn't find chat room " + name + ", creating.");
            chatRoom = new ChatRoom(name);
            BreakerRedditClient client = new BreakerRedditClient();
            boolean subreditExists = client.doesSubredditExist(name);
            if (!subreditExists) {
                Logger.info("Chat room " + name + " doesn't exist on reddit.");
                throw new SubredditDoesNotExistException();
            }
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

    public long getCurrentUserCount() {
        return RedisUtil.getCurrentUserCount(name);
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
        return RedisUtil.getUsernamesPresent(name);
    }

    /**
     * Check if the username is present, loads the list of usernames present.
     * @return
     */
    public boolean isUserPresent(String username) {
        return RedisUtil.isUserPresent(name, username);
    }

    public void userPresent(String username, String connectionId) {
        RedisUtil.userPresent(name, username, connectionId);
    }

    public void userNotPresent(String username, String connectionId) {
        RedisUtil.userNotPresent(name, username, connectionId);
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

    public void cleanupLastMessageReadForUser(String username) {
        Redis.del(new String[]{makeKeyForLastReadTime(name, username)});
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

    /**
     *
     * @param chatUser
     * @param roomJoin Can be null, at which point we'll load it. Useful to provide when optimizing.
     * @param checkBanned Check if user banned as well (requires an extra join)
     * @return
     */
    public boolean userCanPost(ChatUser chatUser, ChatUserRoomJoin roomJoin, boolean checkBanned) {
        if (roomJoin == null) {
            roomJoin = ChatUserRoomJoin.findByUserAndRoom(chatUser, this);
        }
        if (roomJoin == null) {
            return false;
        }
        if (checkBanned) {
            if (getBannedUsers().contains(chatUser)) {
                Logger.debug("User " + chatUser.getUsername() + " is banned from " + name + " and cannot post.");
                return false;
            }
        }
        if (chatUser.getLinkKarma() + chatUser.getCommentKarma() < getKarmaThreshold()) {
//            Logger.debug("User is below karma threshold for " + name);
            return false;
        }
        if (chatUser.isFlagBanned() || chatUser.isShadowBan() || chatUser.isRedditUserSuspended()) {
            Logger.debug("User " + chatUser.getUsername() + " is flag or shadow banned from " + name);
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


    /**
     * Model overrides and other lower level methods
     */

    @Override
    public <T extends JPABase> T save() {
        Logger.info("SAVEOVERRIDE - ChatRoom");
        BreakerCache.removeChatRoom(name);
        // todo Could consider a global server update channel and send updates this way, however this should
        // already currently be handled by the various room update methods elsewhere in the server
//        ChatRoomStream.getEventStream(Constants.CHATROOM_DEFAULT).sendRoomUpdate(this);
        return super.save();
    }
}
