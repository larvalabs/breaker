package models;

import com.larvalabs.linkunfurl.LinkInfo;
import play.data.validation.Unique;
import play.db.jpa.Model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * Links that have been mentioned in messages with link details retrieved by unfurler.
 */
@Entity
@Table(name = "weblink")
public class WebLink extends Model {

    @Unique
    private String url;
    private String title;
    private String description;
    private String heroImageUrl;
    private String videoUrl;
    private String videoType;
    private int videoWidth = -1;
    private int videoHeight = -1;

    public WebLink(String url, LinkInfo linkInfo) {
        this.url = url;
        this.title = linkInfo.getTitle();
        this.description = linkInfo.getDescription();
        this.heroImageUrl = linkInfo.getHeroImageUrl();
        this.videoUrl = linkInfo.getVideoUrl();
        this.videoWidth = linkInfo.getVideoWidth();
        this.videoHeight = linkInfo.getVideoHeight();
    }

    public WebLink(String url, String title, String description, String heroImageUrl, String videoUrl, String videoType, int videoWidth, int videoHeight) {
        this.url = url;
        this.title = title;
        this.description = description;
        this.heroImageUrl = heroImageUrl;
        this.videoUrl = videoUrl;
        this.videoType = videoType;
        this.videoWidth = videoWidth;
        this.videoHeight = videoHeight;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getHeroImageUrl() {
        return heroImageUrl;
    }

    public void setHeroImageUrl(String heroImageUrl) {
        this.heroImageUrl = heroImageUrl;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }

    public String getVideoType() {
        return videoType;
    }

    public void setVideoType(String videoType) {
        this.videoType = videoType;
    }

    public int getVideoWidth() {
        return videoWidth;
    }

    public void setVideoWidth(int videoWidth) {
        this.videoWidth = videoWidth;
    }

    public int getVideoHeight() {
        return videoHeight;
    }

    public void setVideoHeight(int videoHeight) {
        this.videoHeight = videoHeight;
    }

    public LinkInfo getLinkInfo() {
        LinkInfo info = new LinkInfo();
        info.setTitle(title);
        info.setDescription(description);
        info.setHeroImageUrl(heroImageUrl);
        info.setVideoUrl(videoUrl);
        info.setVideoWidth(videoWidth);
        info.setVideoHeight(videoHeight);
        return info;
    }
}
