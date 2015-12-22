package models;

import com.larvalabs.redditchat.util.Util;
import org.hibernate.annotations.Index;
import play.Logger;
import play.db.jpa.Model;

import javax.persistence.Entity;
import javax.persistence.Lob;

@Entity
public class ChatUser extends Model {

    @Index(name = "useruid")
    public String uid;

    public String accessToken;
    public String refreshToken;

    public String username;
    public long linkKarma;
    public long commentKarma;

    @Lob
    public String lastResponseApiMe;

    public ChatUser(String uid) {
        this.uid = uid;
    }

    public static ChatUser get(String uid) {
        return find("uid", uid).first();
    }

    public static ChatUser createNew() {
        ChatUser user = new ChatUser(Util.getShortRandomId());
        user.create();
        return user;
    }

    public static ChatUser findOrCreate(String username) {
        ChatUser user = find("username = ?", username).first();
        if (user == null) {
            Logger.debug("Creating new user object for " + username);
            user = createNew();
        }
        return user;
    }
}