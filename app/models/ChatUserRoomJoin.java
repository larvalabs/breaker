package models;

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
    @Index(name="userindex")
    public ChatUser user;

    @ManyToOne(fetch = FetchType.EAGER)
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
        Query query = JPA.em().createQuery("select userroom from ChatUserRoomJoin userroom join fetch userroom.room where userroom.user = :user");
        query.setParameter("user", chatUser);
        return query.getResultList();
//        return find("user = ? join chatroom", chatUser).fetch();
    }

    public static List<ChatUserRoomJoin> findByChatRoom(ChatRoom chatRoom) {
        return find("room = ?", chatRoom).fetch();
    }

    public long getNumNewMessages() {
        return Message.count("room = ? and id > ?", room, lastSeenMessageId);
    }
}
