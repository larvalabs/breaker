package models;

import com.larvalabs.linkunfurl.LinkInfo;
import com.larvalabs.linkunfurl.LinkUnfurl;
import com.larvalabs.redditchat.Constants;
import com.larvalabs.redditchat.util.Util;
import org.apache.commons.lang.StringUtils;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.Index;
import play.Logger;
import play.db.jpa.JPA;
import play.db.jpa.Model;

import javax.persistence.*;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;

@Entity
@Table(name = "message")
public class Message extends Model {

    public String uuid = Util.getUUID();

    public static final int IMAGETYPE_SCREENSHOT = 0;
    public static final int IMAGETYPE_PROFILE = 1;
    public static final int IMAGETYPE_WALLPAPER = 2;

    @ManyToOne
    @Fetch(FetchMode.JOIN)
    public ChatUser user;

    @ManyToOne
    @Fetch(FetchMode.JOIN)
    public ChatRoom room;

    @Column(length=1000)
    public String messageText;

    public String imageKey;
    public int imageKeyType = IMAGETYPE_SCREENSHOT;

    @Index(name = "createDate")
    public Date createDate = new Date();

    public int flagCount;

    public int likeCount;

    @ManyToMany(cascade = CascadeType.PERSIST)
    @JoinTable(name = "message_like")
    public Set<ChatUser> liked = new HashSet<ChatUser>();

    @ManyToMany(cascade = CascadeType.PERSIST)
    @JoinTable(name = "message_flag")
    public Set<ChatUser> flagged = new HashSet<ChatUser>();

    @ManyToMany(cascade = CascadeType.PERSIST)
    @JoinTable(name = "message_mention")
    public Set<ChatUser> mentioned = new HashSet<ChatUser>();

    @Index(name = "deleted")
    public boolean deleted;

    public boolean hasLinks = false;
    public boolean linksUnfurled = false;

    @ManyToMany(cascade = CascadeType.PERSIST)
    @JoinTable(name = "message_weblink")
    public Set<WebLink> links = new HashSet<>();

    // Scoring related for top message list
    public float score = 0;
    public Date scoreUpdateDate = new Date();

    public String languageDetected;

    public Message(String uuid, ChatUser user, ChatRoom room, String messageText) {
        this.uuid = uuid;
        this.user = user;
        this.room = room;
        this.messageText = messageText;
        this.createDate = new Date();
    }

    public Message(ChatUser user, ChatRoom room, String messageText) {
        this.user = user;
        this.room = room;
        this.messageText = messageText;
        this.createDate = new Date();
    }

    /**
     *
     * @param user
     * @param room
     * @param messageText Usually empty or null for now.
     * @param imageKey
     */
    public Message(ChatUser user, ChatRoom room, String messageText, String imageKey, int imageKeyType) {
        this.user = user;
        this.room = room;
        this.messageText = messageText;
        this.imageKey = imageKey;
        this.imageKeyType = imageKeyType;
        this.createDate = new Date();
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public ChatUser getUser() {
        return user;
    }

    public void setUser(ChatUser user) {
        this.user = user;
    }

    public ChatRoom getRoom() {
        return room;
    }

    public void setRoom(ChatRoom room) {
        this.room = room;
    }

    public String getMessageText() {
        return messageText;
    }

    public void setMessageText(String messageText) {
        this.messageText = messageText;
    }

    public String getImageKey() {
        return imageKey;
    }

    public void setImageKey(String imageKey) {
        this.imageKey = imageKey;
    }

    public int getImageKeyType() {
        return imageKeyType;
    }

    public void setImageKeyType(int imageKeyType) {
        this.imageKeyType = imageKeyType;
    }

    public Date getCreateDate() {
        return createDate;
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

    public int getLikeCount() {
        return likeCount;
    }

    public void setLikeCount(int likeCount) {
        this.likeCount = likeCount;
    }

    public Set<ChatUser> getLiked() {
        return liked;
    }

    public Set<ChatUser> getFlagged() {
        return flagged;
    }

    public void setFlagged(Set<ChatUser> flagged) {
        this.flagged = flagged;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public String getLanguageDetected() {
        return languageDetected;
    }

    public void setLanguageDetected(String languageDetected) {
        this.languageDetected = languageDetected;
    }

    public Set<ChatUser> getMentioned() {
        return mentioned;
    }

    public void setMentioned(Set<ChatUser> mentioned) {
        this.mentioned = mentioned;
    }

    public boolean isHasLinks() {
        return hasLinks;
    }

    public void setHasLinks(boolean hasLinks) {
        this.hasLinks = hasLinks;
    }

    public boolean isLinksUnfurled() {
        return linksUnfurled;
    }

    public void setLinksUnfurled(boolean linksUnfurled) {
        this.linksUnfurled = linksUnfurled;
    }

    public Set<WebLink> getLinks() {
        return links;
    }

    public void setLinks(Set<WebLink> links) {
        this.links = links;
    }

    public void addLink(WebLink link) {
        links.add(link);
    }

    /// Custom stuff

    public boolean flagMessageAndUpdateCounts(ChatUser flaggingUser) {
        if (!flagged.contains(flaggingUser)) {
            flagCount++;
            flagged.add(flaggingUser);
            save();
            user.tryToFlag(flaggingUser);
//            user.setFlagCount(user.getFlagCount() + 1);
//            user.save();
            return true;
        } else {
            return false;
        }
    }

    public boolean addLikingUser(ChatUser likingUser) {
        if (!liked.contains(likingUser) && !this.equals(likingUser)) {
            liked.add(likingUser);
            save();
            return true;
        }
        return false;
    }

    public boolean likeMessageAndUpdateCounts(ChatUser likingUser) {
        if (!liked.contains(likingUser) && !this.equals(likingUser)) {
//            likeCount++;
            liked.add(likingUser);
//            save();
//            user.incrementLikeCount();
//            user.save();
            return true;
        }
        return false;
    }

    public String getDirectS3Url() {
        if (imageKey != null) {
            switch (imageKeyType) {
                case IMAGETYPE_SCREENSHOT: {
                    return Constants.URL_S3_BUCKET_SCREENSHOT_FULLSIZE + imageKey;
                }
                case IMAGETYPE_PROFILE: {
                    return Constants.URL_S3_BUCKET_PROFILE_FULLSIZE + imageKey;
                }
                case IMAGETYPE_WALLPAPER: {
                    return Constants.URL_S3_BUCKET_WALLPAPER_FULLSIZE + imageKey;
                }
            }
        }
        return null;
    }

    public String getImageUrl() {
        String directS3Url = getDirectS3Url();
        if (directS3Url == null) {
            return null;
        } else {
            return directS3Url;
        }
    }

    public String getImageThumbUrl() {
        String regImageUrl = getDirectS3Url();
        if (regImageUrl == null) {
            return null;
        } else {
            return Constants.URL_WALLPAPER_THUMB + regImageUrl;
        }
    }

    public List<ChatUser> getMentionedUsers() {
        ArrayList<ChatUser> mentionList = new ArrayList<ChatUser>();
        if (messageText == null) {
            return mentionList;
        }
        List<String> mentionedUsernames = getMentionedUsernames(messageText);
        for (String mentionedUsername : mentionedUsernames) {
            ChatUser mentionedUser = ChatUser.findByUsername(mentionedUsername);
            if (mentionedUser != null) {
                mentionList.add(mentionedUser);
            }
        }
        return mentionList;
    }

    public static List<String> getMentionedUsernames(String messageText) {
        ArrayList<String> usernames = new ArrayList<>();
        Matcher matcher = ChatUser.PATTERN_USER_MENTION.matcher(messageText);
        while (matcher.find()) {
            String username = matcher.group();
            Logger.info("Found username: " + username);
            username = username.replaceAll("@", "").trim().toLowerCase();
            usernames.add(username);
        }
        return usernames;
    }

    public Set<ChatUser> updateMentionedUsers() {
        List<ChatUser> mentionedUsers = getMentionedUsers();
        mentioned = new HashSet<ChatUser>(mentionedUsers);
        save();
        return mentioned;
    }

    public static Message findByUUID(String uuid) {
        return Message.find("uuid = ?", uuid).first();
    }

    public static class MentionMessage {
        public String replacedMessage;
        public List<String> usernames;

        public MentionMessage(String replacedMessage, List<String> usernames) {
            this.replacedMessage = replacedMessage;
            this.usernames = usernames;
        }
    }

    public static MentionMessage getMessageTextWithUsernamePlaceholders(String messageText) {
        ArrayList<String> usernames = new ArrayList<String>();
        Matcher matcher = ChatUser.PATTERN_USER_MENTION.matcher(messageText);
        String replacedMsg = new String(messageText);
        while (matcher.find()) {
            String usernameWithSymbol = matcher.group().toLowerCase().trim();
            if (!usernames.contains(usernameWithSymbol)) {
                replacedMsg = replacedMsg.replaceAll(usernameWithSymbol, "@" + usernames.size());
                Logger.info("Found username: " + usernameWithSymbol);
//            String username = usernameWithSymbol.replaceAll("@", "");
                usernames.add(usernameWithSymbol);
            }
        }

        return new MentionMessage(replacedMsg, usernames);
    }

    public static String reconstructMessageWithTranslation(MentionMessage mentionMessage, String translatedMsg) {
        String reconstructedMsg = new String(translatedMsg);
        for (int i = 0; i < mentionMessage.usernames.size(); i++) {
            String username = mentionMessage.usernames.get(i);
            reconstructedMsg = reconstructedMsg.replace("@" + i, username);
        }
        return reconstructedMsg;
    }

    public boolean didUserLike(ChatUser loggedInUser) {
        return liked.contains(loggedInUser);
    }

    public void recalcScore() {
        Date curDate = new Date();
        long ageMillis = curDate.getTime() - createDate.getTime();
        double ageHours = ageMillis / 3600000f;
        score = (float) (Math.pow(likeCount, 0.8d) / Math.pow(ageHours + 1, 0.3d));
        scoreUpdateDate = new Date();
    }

    public boolean isImageMessage() {
        return imageKey != null;
    }

    public boolean hasDetectedLanguage() {
        return !StringUtils.isBlank(getLanguageDetected());
    }

    public boolean isLanguageSame(String userLanguage) {
        return (getLanguageDetected() != null && getLanguageDetected().equals(userLanguage));
    }

    public static List<Message> getLiked(ChatUser user, List<Message> messages) {
        if (messages == null || messages.size() == 0) {
            return new ArrayList<Message>();
        }
        Query query = JPA.em().createQuery("FROM Message m LEFT JOIN m.liked l WHERE m in :messages and :user in l");
        query.setParameter("user", user);
        query.setParameter("messages", messages);
        List<Object[]> likeResult = query.getResultList();
        List<Message> msgResultList = new ArrayList<Message>();
        for (Object[] messageAndUser : likeResult) {
            msgResultList.add((Message) messageAndUser[0]);
        }
        return msgResultList;
    }

    public void unfurlLinks() {
        List<String> links = Util.getLinks(messageText);
        if (links.size() > 0) {
            setHasLinks(true);
            for (String link : links) {
                try {
                    LinkInfo linkInfo = LinkUnfurl.unfurl(link, 10000);
                    WebLink weblink = new WebLink(link, linkInfo);
                    weblink.save();
                    addLink(weblink);
                } catch (IOException e) {
                    Logger.error("Error unfurling link: " + link);
                }
            }
        }
        setLinksUnfurled(true);
    }

}
