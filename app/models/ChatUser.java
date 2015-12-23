package models;

import com.larvalabs.redditchat.Constants;
import com.larvalabs.redditchat.util.Util;
import org.apache.commons.lang.StringUtils;
import org.hibernate.annotations.Index;
import play.Logger;
import play.Play;
import play.db.DB;
import play.db.jpa.JPA;
import play.db.jpa.Model;

import javax.persistence.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Entity
@Table(name = "chatuser")
public class ChatUser extends Model {

    public static final String PATTERN_USERNAME_BASE = "([A-Za-z0-9_-]{1,20})";
    public static final Pattern PATTERN_VALID_USER = Pattern.compile(PATTERN_USERNAME_BASE);
    public static final Pattern PATTERN_USER_MENTION = Pattern.compile("@" + PATTERN_USERNAME_BASE);

    @Column(unique = true)
    public String uid;

    public String accessToken;
    public String refreshToken;

    public String username;
    public long linkKarma;
    public long commentKarma;

    public Date createDate = new Date();

    public int flagCount;

    public long likeCount;

    public String profileImageKey;

    @ManyToMany(cascade = CascadeType.PERSIST)
    @JoinTable(name = "user_starredroom")
    public Set<ChatRoom> starredRooms = new HashSet<ChatRoom>();

    @ManyToMany(cascade = CascadeType.PERSIST)
    @JoinTable(name = "user_watchroom")
    public Set<ChatRoom> watchedRooms = new HashSet<ChatRoom>();

    @ManyToMany(cascade = CascadeType.PERSIST)
    @JoinTable(name = "user_flagging_user")
    public Set<ChatUser> flaggingUsers = new HashSet<ChatUser>();

    public long lastSeenMentionedMessageId = -1;

    public boolean shadowBan = false;

    @Lob
    public String lastResponseApiMe;

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

    public String getProfileImageUrl() {
        if (profileImageKey != null) {
            return Constants.URL_S3_BUCKET_PROFILE_FULLSIZE + profileImageKey;
        }
        return null;
    }

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
        }
        return user;
    }

    public static ChatUser findByEmail(String email) {
        return ChatUser.find("email = ?", email).first();
    }

    public static ChatUser findByUsername(String username) {
        return ChatUser.find("username", username).first();
    }

    public void joinChatRoom(ChatRoom chatRoom) {
        ChatUserRoomJoin join = ChatUserRoomJoin.findByUserAndRoom(this, chatRoom);
        if (join != null) {
            return;
        }

        join = new ChatUserRoomJoin(this, chatRoom);
        join.save();
        chatRoom.numberOfUsers++;
        chatRoom.save();
        Logger.info("User " + username + ": " + chatRoom.name + " : " + chatRoom.numberOfUsers);
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
        return starredRooms.contains(room);
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

    public void watchRoom(ChatRoom room) {
        watchedRooms.add(room);
        save();
    }

    public void stopWatching(ChatRoom room) {
        watchedRooms.remove(room);
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
}