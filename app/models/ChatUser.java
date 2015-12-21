package models;

import play.db.jpa.Model;

import javax.persistence.Entity;
import javax.persistence.Lob;

@Entity
public class ChatUser extends Model {

    public long uid;
    public String accessToken;
    public String refreshToken;
    public String localToken;
    public String username;
    public long linkKarma;
    public long commentKarma;

    @Lob
    public String lastResponseApiMe;

    public ChatUser(long uid) {
        this.uid = uid;
    }

    public static ChatUser get(long id) {
        return find("uid", id).first();
    }

    public static ChatUser createNew() {
        long uid = (long) Math.floor(Math.random() * 10000);
        ChatUser user = new ChatUser(uid);
        user.create();
        return user;
    }

}