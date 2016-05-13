package models;

import org.hibernate.annotations.Index;
import play.data.validation.Unique;
import play.db.jpa.Model;

import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * Created by matt on 5/13/16.
 */
@Entity
@Table(name = "optoutuser")
public class OptOutUser extends Model {

    @Unique
    @Index(name = "optoutusername")
    public String username;

    public OptOutUser(String username) {
        this.username = username;
    }

    public static boolean didOptOut(String username) {
        return count("byUsernameLike", username.toLowerCase()) > 0;
    }

    public static void addOptOut(String username) {
        if (!didOptOut(username)) {
            new OptOutUser(username).save();
        }
    }
}
