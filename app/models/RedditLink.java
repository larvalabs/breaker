package models;

import play.data.validation.Unique;
import play.db.jpa.Model;

import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.Table;

/**
 * Links that have been posted to channels by bots.
 * Used to avoid dupes and to determine what's new.
 */
@Entity
@Table(name = "redditlink")
public class RedditLink extends Model {

    @Unique
    private String redditId;
    private String subreddit;
    private String permalink;
    private String url;
    @Lob
    private String title;
    @Lob
    private String json;

    public RedditLink(String redditId, String subreddit, String permalink, String url, String title, String json) {
        this.redditId = redditId;
        this.subreddit = subreddit;
        this.permalink = permalink;
        this.url = url;
        this.title = title;
        this.json = json;
    }

    public String getRedditId() {
        return redditId;
    }

    public void setRedditId(String redditId) {
        this.redditId = redditId;
    }

    public String getSubreddit() {
        return subreddit;
    }

    public void setSubreddit(String subreddit) {
        this.subreddit = subreddit;
    }

    public String getPermalink() {
        return permalink;
    }

    public void setPermalink(String permalink) {
        this.permalink = permalink;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getJson() {
        return json;
    }

    public void setJson(String json) {
        this.json = json;
    }

    // Custom stuff

    public static RedditLink findByRedditId(String redditId) {
        return find("redditId", redditId).first();
    }
}
