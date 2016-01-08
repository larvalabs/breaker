package models;

import com.larvalabs.redditchat.Constants;
import com.larvalabs.redditchat.dataobj.JsonUser;
import play.Logger;
import play.db.DB;
import play.db.jpa.Model;
import play.modules.redis.Redis;

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

    @Column(unique = true)
    public String name;

    public String iconUrl;
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

    public ChatRoom(String name) {
        this.name = name;
        this.numberOfUsers = 0;
    }

    public String getName() {
        return name;
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

    // Do stuff zone

    public static ChatRoom findByName(String name) {
        return find("byNameLike", name.toLowerCase()).first();
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
        return Message.find("room = ? order by id desc", this).fetch(limit);
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
        return users;
    }

    public static ChatRoom findOrCreateForName(String name) {
        ChatRoom chatRoom = findByName(name);
        if (chatRoom == null) {
            Logger.info("Couldn't find chat room " + name + ", creating.");
            chatRoom = new ChatRoom(name);
            chatRoom.save();
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

    private String getRedisPresenceKeyForRoom() {
        return "presence_" + name;
    }

    public long getCurrentUserCount() {
        try {
            int time = (int) (System.currentTimeMillis() / 1000);
            Long zcount = Redis.zcount(getRedisPresenceKeyForRoom(), time - Constants.PRESENCE_TIMEOUT_SEC, time);
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
            users[i] = JsonUser.fromUser(presentUserObject);
            i++;
        }
        return users;
    }

    // note: could consider doing this as a separate set of just usernames
    public TreeSet<String> getUsernamesPresent() {
        try {
            int time = (int) (System.currentTimeMillis() / 1000);
            Set<String> usersPresent = Redis.zrangeByScore(getRedisPresenceKeyForRoom(), time - Constants.PRESENCE_TIMEOUT_SEC, time);
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
            Logger.error("Error contacting redis.");
            return new TreeSet<String>();
        }
    }


    public boolean isUserPresent(ChatUser user) {
        TreeSet<String> usernamesPresent = getUsernamesPresent();
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

    public void userPresent(ChatUser user, String connectionId) {
        try {
            int time = (int) (System.currentTimeMillis() / 1000);
            Redis.zadd(getRedisPresenceKeyForRoom(), time, getUsernameAndConnectionString(user, connectionId));
            cleanPresenceSet();             // todo remove later
        } catch (Exception e) {
            Logger.error("Error contacting redis.");
        }
    }

    private String getUsernameAndConnectionString(ChatUser user, String connectionId) {
        return user.username + ":" + connectionId;
    }

    /**
     *
     * @param [0] = username, [1] = connectionId
     * @return
     */
    private String[] splitUsernameAndConnection(String combined) {
        return combined.split(":");
    }

    public void userNotPresent(ChatUser user, String connectionId) {
        try {
            Redis.zrem(getRedisPresenceKeyForRoom(), getUsernameAndConnectionString(user, connectionId));
        } catch (Exception e) {
            Logger.error(e, "Error contacting redis.");
        }
    }

    public void cleanPresenceSet() {
        try {
            int time = (int) (System.currentTimeMillis() / 1000);
            Long removed = Redis.zremrangeByScore(getRedisPresenceKeyForRoom(), 0, time - Constants.PRESENCE_TIMEOUT_SEC * 2);
//            Logger.debug("Clean of presence set for " + name + " removed " + removed + " elements.");
        } catch (Exception e) {
            Logger.error(e, "Error contacting redis.");
        }
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
}
