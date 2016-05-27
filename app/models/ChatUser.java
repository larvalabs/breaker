package models;

import com.google.gson.JsonObject;
import com.larvalabs.redditchat.Constants;
import com.larvalabs.redditchat.dataobj.JsonFlair;
import com.larvalabs.redditchat.util.RedisUtil;
import com.larvalabs.redditchat.util.Util;
import jobs.UpdateUserFromRedditJob;
import org.apache.commons.lang.StringUtils;
import org.hibernate.annotations.Index;
import play.Logger;
import play.Play;
import play.db.DB;
import play.db.jpa.JPA;
import play.db.jpa.Model;
import reddit.BreakerRedditClient;
import reddit.RedditRequestError;

import javax.persistence.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.regex.Pattern;

@Entity
@Table(name = "chatuser")
public class ChatUser extends Model {

    public static final String PATTERN_USERNAME_BASE = "([A-Za-z0-9_-]{1,20})";
    public static final Pattern PATTERN_VALID_USER = Pattern.compile(PATTERN_USERNAME_BASE);
    public static final Pattern PATTERN_USER_MENTION = Pattern.compile("@" + PATTERN_USERNAME_BASE);

    public static final String PREFVAL_NOTIFICATION_EVERYTHING = "everything";
    public static final String PREFVAL_NOTIFICATION_STARRED = "starred";
    public static final String PREFVAL_NOTIFICATION_MENTIONED = "mentioned";
    public static final String PREFVAL_NOTIFICATION_NEVER = "never";

    @Column(unique = true)
    public String uid;

    public boolean admin = false;

    public String accessToken;
    public String refreshToken;

    @Column(unique = true)
    @Index(name = "username")
    public String username;
    public String email;
    public long linkKarma;
    public long commentKarma;
    public long redditUserCreatedUTC = -1;
    public boolean redditUserSuspended = false;

    public Date createDate = new Date();
    public Date lastSeenDate = createDate;

    public int flagCount;

    public long likeCount;

    public String profileImageKey;

    public String notificationPreference = PREFVAL_NOTIFICATION_EVERYTHING;

    @ManyToMany(cascade = CascadeType.PERSIST)
    @JoinTable(name = "user_starredroom")
    public Set<ChatRoom> starredRooms = new HashSet<ChatRoom>();

    @ManyToMany(cascade = CascadeType.PERSIST)
    @JoinTable(name = "user_watchroom")
    public Set<ChatRoom> watchedRooms = new HashSet<ChatRoom>();

    @ManyToMany(cascade = CascadeType.PERSIST)
    @JoinTable(name = "user_moderatorroom")
    public Set<ChatRoom> moderatedRooms = new HashSet<ChatRoom>();

    @ManyToMany(cascade = CascadeType.PERSIST)
    @JoinTable(name = "user_flagging_user")
    public Set<ChatUser> flaggingUsers = new HashSet<ChatUser>();

    public long lastSeenMentionedMessageId = -1;

    public boolean shadowBan = false;

    @Column(length=10000)
    public String lastResponseApiMe;

    public String statusMessage;

    public boolean bot;

    public ChatUser(String uid) {
        this.uid = uid;
    }

    public static ChatUser get(String uid) {
        return find("uid", uid).first();
    }

    public String getProfileImageKey() {
        return profileImageKey;
    }

    public void setProfileImageKey(String profileImageKey) {
        this.profileImageKey = profileImageKey;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public boolean isAdmin() {
        return admin;
    }

    public void setAdmin(boolean admin) {
        this.admin = admin;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public long getLinkKarma() {
        return linkKarma;
    }

    public void setLinkKarma(long linkKarma) {
        this.linkKarma = linkKarma;
    }

    public long getCommentKarma() {
        return commentKarma;
    }

    public void setCommentKarma(long commentKarma) {
        this.commentKarma = commentKarma;
    }

    public long getRedditUserCreatedUTC() {
        return redditUserCreatedUTC;
    }

    public void setRedditUserCreatedUTC(long redditUserCreatedUTC) {
        this.redditUserCreatedUTC = redditUserCreatedUTC;
    }

    public boolean isRedditUserSuspended() {
        return redditUserSuspended;
    }

    public void setRedditUserSuspended(boolean redditUserSuspended) {
        this.redditUserSuspended = redditUserSuspended;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public Date getLastSeenDate() {
        return lastSeenDate;
    }

    public void setLastSeenDate(Date lastSeenDate) {
        this.lastSeenDate = lastSeenDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    public int getFlagCount() {
        return flagCount;
    }

    public void setFlagCount(int flagCount) {
        this.flagCount = flagCount;
    }

    public long getLikeCount() {
        return likeCount;
    }

    public void setLikeCount(long likeCount) {
        this.likeCount = likeCount;
    }

    public Set<ChatRoom> getStarredRooms() {
        return starredRooms;
    }

    public void setStarredRooms(Set<ChatRoom> starredRooms) {
        this.starredRooms = starredRooms;
    }

    public Set<ChatRoom> getWatchedRooms() {
        return watchedRooms;
    }

    public void setWatchedRooms(Set<ChatRoom> watchedRooms) {
        this.watchedRooms = watchedRooms;
    }

    public Set<ChatRoom> getModeratedRooms() {
        return moderatedRooms;
    }

    public void setModeratedRooms(Set<ChatRoom> moderatedRooms) {
        this.moderatedRooms = moderatedRooms;
    }

    public Set<ChatUser> getFlaggingUsers() {
        return flaggingUsers;
    }

    public void setFlaggingUsers(Set<ChatUser> flaggingUsers) {
        this.flaggingUsers = flaggingUsers;
    }

    public long getLastSeenMentionedMessageId() {
        return lastSeenMentionedMessageId;
    }

    public void setLastSeenMentionedMessageId(long lastSeenMentionedMessageId) {
        this.lastSeenMentionedMessageId = lastSeenMentionedMessageId;
    }

    public boolean isShadowBan() {
        return shadowBan;
    }

    public void setShadowBan(boolean shadowBan) {
        this.shadowBan = shadowBan;
    }

    public String getLastResponseApiMe() {
        return lastResponseApiMe;
    }

    public void setLastResponseApiMe(String lastResponseApiMe) {
        this.lastResponseApiMe = lastResponseApiMe;
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    public void setStatusMessage(String statusMessage) {
        this.statusMessage = statusMessage;
    }

    public String getProfileImageUrl() {
        if (profileImageKey != null) {
            return Constants.URL_S3_BUCKET_PROFILE_FULLSIZE + profileImageKey;
        }
        return Constants.DEFAULT_PROFILE_URL;
    }

    public String getNotificationPreference() {
        return notificationPreference;
    }

    public void setNotificationPreference(String notificationPreference) {
        this.notificationPreference = notificationPreference;
    }

    public boolean isNotificationEnabledForEverything() {
        if (StringUtils.isBlank(notificationPreference)) {
            return true;
        } else if (notificationPreference.equals(PREFVAL_NOTIFICATION_EVERYTHING)) {
            return true;
        }
        return false;
    }

    public boolean isNotificationEnabledForStarred() {
        if (StringUtils.isBlank(notificationPreference)) {
            return true;
        } else if (isNotificationEnabledForEverything() || notificationPreference.equals(PREFVAL_NOTIFICATION_STARRED)) {
            return true;
        }
        return false;
    }

    public boolean isNotificationEnabledForMention() {
        if (StringUtils.isBlank(notificationPreference)) {
            return true;
        } else if (isNotificationEnabledForEverything() || isNotificationEnabledForStarred() ||
                notificationPreference.equals(PREFVAL_NOTIFICATION_MENTIONED)) {
            return true;
        }
        return false;
    }

    public boolean isBot() {
        return bot;
    }

    public void setBot(boolean bot) {
        this.bot = bot;
    }

    // Static stuff

    public static ChatUser createNew() {
        ChatUser user = new ChatUser(Util.getShortRandomId());
        user.create();
        return user;
    }

    public static ChatUser findOrCreate(String username) {
        ChatUser user = findByUsername(username);
        if (user == null) {
            Logger.debug("Creating new user object for " + username);
            user = createNew();
            user.setUsername(username);
            user.save();
        }
        return user;
    }

    public static ChatUser findByEmail(String email) {
        return ChatUser.find("email = ?", email).first();
    }

    public static ChatUser findByUsername(String username) {
        if (username == null) {
            return null;
        }
        username = username.toLowerCase().trim();
        return ChatUser.find("LOWER(username) = ?", username).first();
    }

    public void joinChatRoom(ChatRoom chatRoom) throws UserBannedException, NoAccessToPrivateRoomException, UnableToCheckAccessToPrivateRoom {
        if (chatRoom == null) {
            Logger.warn("Chat room was null, cannot join.");
            return;
        }

        if (chatRoom.getBannedUsers().contains(this)) {
            Logger.warn("User " + username + " can't join room because banned: " + chatRoom.getName());
            throw new UserBannedException();
        }

        ChatUserRoomJoin join = ChatUserRoomJoin.findByUserAndRoom(this, chatRoom);
        if (join != null) {
            return;
        }

        if (chatRoom.isPrivateRoom()) {
            Logger.info("User " + username + " trying to join private room " + chatRoom.getName());
            BreakerRedditClient client = new BreakerRedditClient();
            try {
                boolean doesUserHaveAccessToSubreddit = client.doesUserHaveAccessToSubreddit(this, chatRoom.getName());
                if (!doesUserHaveAccessToSubreddit) {
                    Logger.info("User " + username + " does not have access to " + chatRoom.getName());
                    throw new NoAccessToPrivateRoomException();
                }
            } catch (RedditRequestError redditRequestError) {
                throw new UnableToCheckAccessToPrivateRoom(redditRequestError);
            }
        }

        join = new ChatUserRoomJoin(this, chatRoom);
        join.save();
        chatRoom.numberOfUsers++;
        chatRoom.save();
        Logger.info("User " + username + ": " + chatRoom.name + " : " + chatRoom.numberOfUsers);

        new UpdateUserFromRedditJob(getId()).afterRequest();
    }

    public void leaveChatRoom(ChatRoom chatRoom) {
        ChatUserRoomJoin join = ChatUserRoomJoin.findByUserAndRoom(this, chatRoom);
        if (join == null) {
            Logger.debug("Not a member of that room.");
            return;
        }

        join.delete();
        Logger.info(username + " no longer member of " + chatRoom.name);
    }

    public List<ChatUserRoomJoin> getChatRoomJoins() {
        return ChatUserRoomJoin.findByUser(this);
    }

    public void starRoom(ChatRoom chatRoom) {
        starredRooms.add(chatRoom);
    }

    public void unstarRoom(ChatRoom chatRoom) {
        starredRooms.remove(chatRoom);
    }

    public boolean isRoomStarred(ChatRoom room) {
        return false;
        // todo this has a JPA error when being called from websocket due to no session
//        return starredRooms != null && starredRooms.contains(room);
    }

    public void markAllRoomsAsRead() {
        List<ChatUserRoomJoin> joins = ChatUserRoomJoin.findByUser(this);
        for (ChatUserRoomJoin join : joins) {
            join.room.markMessagesSeen(this);
        }
    }

    public boolean isNotBanned() {
        return flagCount < Constants.USER_FLAG_THRESHOLD && !shadowBan;
    }

    public boolean isFlagBanned() {
        return flagCount >= Constants.THRESHOLD_USER_FLAG;
    }

    public boolean isModerator(ChatRoom chatRoom) {
        return admin || getModeratedRooms().contains(chatRoom);
    }

    public void watchRoom(ChatRoom room) {
        watchedRooms.add(room);
        save();
    }

    public void stopWatching(ChatRoom room) {
        watchedRooms.remove(room);
        save();
    }

    public void moderateRoom(ChatRoom room) {
        moderatedRooms.add(room);
        save();
    }

    public void stopModerating(ChatRoom room) {
        moderatedRooms.remove(room);
        save();
    }

    public List<ChatRoom> getTopChatRooms(int limit) {
        if (!Play.runingInTestMode()) {
            // NOTE: Sadly this is not tested because in memory DB can't handle the query
            Query query = JPA.em().createQuery("select max(m), sum(1) as messagecount from Message m where user=:user group by room order by messagecount desc").setParameter("user", this);
            List<Object[]> messages = query.getResultList();
            List<ChatRoom> topRooms = new ArrayList<ChatRoom>();
            for (Object[] queryObj : messages) {
                topRooms.add(((Message) queryObj[0]).room);
                if (topRooms.size() == limit) {
                    break;
                }
            }
            return topRooms;
        } else {
            Logger.info("Lame test mode for this query because the in memory DB can't handle the sum");
            HashSet<ChatRoom> rooms = new HashSet<ChatRoom>();
            List<Message> messages = Message.find("user = ?", this).fetch();
            for (Message message : messages) {
                rooms.add(message.getRoom());
            }
            return new ArrayList<ChatRoom>(rooms);
        }
    }

    public List<Message> getLatestMessages(int limit) {
        return Message.find("user = ? and deleted = false and flagCount < "
                + Constants.THRESHOLD_MESSAGE_FLAG + " order by createDate desc", this).fetch(limit);
    }

    public List<Message> getMentioned(int limit) {
        return Message.find("SELECT DISTINCT m from Message m JOIN m.mentioned u where u = ? and m.deleted = false and m.flagCount < "
                + Constants.THRESHOLD_MESSAGE_FLAG + " order by m.createDate desc", this).fetch(limit);
    }

    public boolean tryToFlag(ChatUser flaggingUser) {
        if (flaggingUser.equals(this)) {
            Logger.info("User is the same, can't flag.");
            return false;
        }
        for (ChatUser user : flaggingUsers) {
            if (user.equals(flaggingUser)) {
                Logger.info("User " + flaggingUser.username + " has already flagged user " + username + " in the past, not flagging.");
                return false;
            }
        }
        flaggingUsers.add(flaggingUser);
        flagCount++;
        save();
        return true;
    }

    public static List<ChatUser> getTopUsers(int days, int limit, ChatRoom room) {
        ArrayList<ChatUser> topUsers = new ArrayList<ChatUser>();
        if (Play.runingInTestMode()) {
            return topUsers;
        }
        String sql;
        if (room == null) {
            Logger.info("Top global users:");
            sql = "select max(u.id), sum(m.likeCount) as count from message m, chatuser u where m.user_id=u.id " +
                    "and m.createDate >= ( NOW() - INTERVAL '" + days + " DAY' ) group by user_id order by count desc limit " + limit;
        } else {
            Logger.info("Top users for room " + room.name);
            sql = "select max(u.id), sum(m.likeCount) as count from message m, chatuser u where m.user_id=u.id " +
                    "and m.createDate >= ( NOW() - INTERVAL '" + days + " DAY' ) and m.room_id=" + room.getId() + " group by user_id order by count desc limit " + limit;
        }
        final ResultSet resultSet = DB.executeQuery(sql);
        try {
            while (resultSet.next()) {
                Long userId = resultSet.getLong(1);
                int likeCount = resultSet.getInt(2);
                if (likeCount > 0) {
                    ChatUser user = ChatUser.findById(userId);
                    if (user != null) {
                        Logger.info(user.username + " with " + likeCount + " likeCount");
                        topUsers.add(user);
                    } else {
                        Logger.warn("Couldn't find user for id " + userId);
                    }
                }
            }
        } catch (SQLException e) {
            Logger.error(e, "Problem getting top users.");
            return topUsers;
        } finally {
            if (resultSet != null) {
                try {
                    resultSet.close();
                } catch (SQLException e) {
                    Logger.error(e, "Error getting unread counts.");
                }
            }
        }
        return topUsers;
    }

    public static ChatUser getBreakerBot() {
        ChatUser user = findByUsername(Constants.BREAKER_BOT_USERNAME);
        if (user == null) {
            user = new ChatUser(Util.getUUID());
            user.setUsername(Constants.BREAKER_BOT_USERNAME);
            user.save();
        }
        return user;
    }

    public static ChatUser getSystemUser() {
        ChatUser user = findByUsername(Constants.SYSTEM_USERNAME);
        if (user == null) {
            user = new ChatUser(Util.getUUID());
            user.setUsername(Constants.SYSTEM_USERNAME);
            user.save();
        }
        return user;
    }

    public boolean isGuest() {
        return Constants.USERNAME_GUEST.equals(username);
    }

    public void deleteAllMessages() {
        // A bit inefficient, but we won't call this much (hopefully)
        List<Message> messages = Message.find("user = ?", this).fetch(100);
        for (Message message : messages) {
            message.setDeleted(true);
            message.save();
        }
    }

    public HashMap<String, JsonFlair> getFlairAsJson() {
        HashMap<String, JsonFlair> map = new HashMap<>();
        List<ChatUserRoomJoin> joins = ChatUserRoomJoin.findByUser(this);
        for (ChatUserRoomJoin join : joins) {
            map.put(join.getRoom().getName(), new JsonFlair(join.flairText, join.flairCss, join.flairPosition));
        }
        return map;
    }

    public static List<ChatUser> findAdmins() {
        return find("byAdmin", true).fetch();
    }

    public class UserBannedException extends Exception {
    }

    public class NoAccessToPrivateRoomException extends Exception {
    }

    public class UnableToCheckAccessToPrivateRoom extends Exception {
        public RedditRequestError redditRequestError;

        public UnableToCheckAccessToPrivateRoom(RedditRequestError redditRequestError) {
            this.redditRequestError = redditRequestError;
        }
    }

    @Override
    public String toString() {
        return "User: " + getId() + ":" + getUsername();
    }

    public boolean isUserOnlineInAnyRoom() {
        return RedisUtil.isUserOnlineInAnyRoom(username);
    }

    public void updateUserFromRedditJson(JsonObject userJson) {
        linkKarma = userJson.get("link_karma").getAsLong();
        commentKarma = userJson.get("comment_karma").getAsLong();
        redditUserCreatedUTC = userJson.get("created_utc").getAsLong();
        redditUserSuspended = userJson.get("is_suspended").getAsBoolean();
        lastResponseApiMe = userJson.toString();
    }
}
