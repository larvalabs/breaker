package models;

import org.apache.commons.lang.StringUtils;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.Index;
import play.db.jpa.JPA;
import play.db.jpa.Model;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "userroom")
public class ChatUserRoomJoin extends Model {

    @ManyToOne
    @Fetch(FetchMode.JOIN)
    @Index(name="userindex")
    public ChatUser user;

    @ManyToOne
    @Fetch(FetchMode.JOIN)
    @Index(name="roomindex")
    public ChatRoom room;

    public long lastSeenMessageId;
    public long lastSeenMessageTime;

    public String flairText;
    public String flairCss;
    public String flairPosition;

    public ChatUserRoomJoin(ChatUser user, ChatRoom room) {
        this.user = user;
        this.room = room;
        this.lastSeenMessageId = -1;
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

    public long getLastSeenMessageId() {
        return lastSeenMessageId;
    }

    public void setLastSeenMessageId(long lastSeenMessageId) {
        this.lastSeenMessageId = lastSeenMessageId;
    }

    public long getLastSeenMessageTime() {
        return lastSeenMessageTime;
    }

    public void setLastSeenMessageTime(long lastSeenMessageTime) {
        this.lastSeenMessageTime = lastSeenMessageTime;
    }

    public String getFlairText() {
        return flairText;
    }

    public void setFlairText(String flairText) {
        this.flairText = flairText;
    }

    public String getFlairCss() {
        return flairCss;
    }

    public void setFlairCss(String flairCss) {
        this.flairCss = flairCss;
    }

    public String getFlairPosition() {
        return flairPosition;
    }

    public void setFlairPosition(String flairPosition) {
        this.flairPosition = flairPosition;
    }

    ///

    public static ChatUserRoomJoin findByUserAndRoom(ChatUser chatUser, ChatRoom chatRoom) {
        return find("user = ? and room = ?", chatUser, chatRoom).first();
    }

    public static List<ChatUserRoomJoin> findByUser(ChatUser chatUser) {
        Query query = JPA.em().createQuery("select userroom from ChatUserRoomJoin userroom join fetch userroom.room where userroom.user = :userparam");
        query.setParameter("userparam", chatUser);
        return query.getResultList();
//        return find("user = ? join chatroom", chatUser).fetch();
    }

    public static List<ChatUserRoomJoin> findWithoutDeletedAndUnopened(ChatUser user) {
        Query getAllStuffQuery = JPA.em().createQuery("select ur from ChatUserRoomJoin ur join fetch ur.room urr join fetch ur.user u where ur.room " +
                "in (select room from ChatUserRoomJoin ur2 where ur2.user = :user) " +
                "and ur.room.deleted = false and ur.room.open = true")
                .setParameter("user", user);
        return getAllStuffQuery.getResultList();
    }

    public static List<ChatUserRoomJoin> findByChatRoom(ChatRoom chatRoom) {
        return find("room = ?", chatRoom).fetch();
    }

    public long getNumNewMessages() {
        return Message.count("room = ? and createDate > ?", room, new Date(lastSeenMessageTime));
    }

    public long getNumNewMessagesIgnoringBots() {
        return Message.count("room = ? and createDate > ? and user.bot = false", room, new Date(lastSeenMessageTime));
    }

    public boolean updateFlairIfDifferent(String flairText, String flairCss, String flairPosition) {
        boolean changed = false;
        if (!StringUtils.equals(this.flairText, flairText)) {
            this.flairText = flairText;
            changed = true;
        }
        if (!StringUtils.equals(this.flairCss, flairCss)) {
            this.flairCss = flairCss;
            changed = true;
        }
        if (!StringUtils.equals(this.flairPosition, flairPosition)) {
            this.flairPosition = flairPosition;
            changed = true;
        }
        return changed;
    }
}
