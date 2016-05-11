package models;

import com.larvalabs.linkunfurl.LinkInfo;
import com.larvalabs.redditchat.dataobj.JsonLinkInfo;
import com.larvalabs.redditchat.util.Util;
import play.data.validation.Unique;
import play.db.jpa.Model;

import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * Links that have been mentioned in messages with link details retrieved by unfurler.
 */
@Entity
@Table(name = "weblink")
public class WebLink extends Model {

    private String uuid;
    @Unique
    private String url;
    private String type;
    private String site;
    private String title;
    private String description;
    private String imageUrl;
    private int imageWidth = -1;
    private int imageHeight = -1;
    private long imageSize = -1;
    private String videoUrl;
    private String videoType;
    private int videoWidth = -1;
    private int videoHeight = -1;

    public WebLink(String url, LinkInfo linkInfo) {
        this.uuid = Util.getUUID();
        this.url = url;
        this.type = linkInfo.getType();
        this.site = linkInfo.getSite();
        this.title = linkInfo.getTitle();
        this.description = linkInfo.getDescription();
        this.imageUrl = linkInfo.getImageUrl();
        if (linkInfo.getImageWidth() != null) {
            this.imageWidth = linkInfo.getImageWidth();
        }
        if (linkInfo.getImageHeight() != null) {
            this.imageHeight = linkInfo.getImageHeight();
        }
        if (linkInfo.getImageSize() != null) {
            this.imageSize = linkInfo.getImageSize();
        }
        this.videoUrl = linkInfo.getVideoUrl();
        if (linkInfo.getVideoWidth() != null) {
            this.videoWidth = linkInfo.getVideoWidth();
        }
        if (linkInfo.getVideoHeight() != null) {
            this.videoHeight = linkInfo.getVideoHeight();
        }
    }

    public WebLink(String url, String type, String site, String title, String description,
                   String imageUrl, int imageWidth, int imageHeight, long imageSize,
                   String videoUrl, String videoType, int videoWidth, int videoHeight) {
        this.uuid = Util.getUUID();
        this.url = url;
        this.type = type;
        this.site = site;
        this.title = title;
        this.description = description;
        this.imageUrl = imageUrl;
        this.imageWidth = imageWidth;
        this.imageHeight = imageHeight;
        this.imageSize = imageSize;
        this.videoUrl = videoUrl;
        this.videoType = videoType;
        this.videoWidth = videoWidth;
        this.videoHeight = videoHeight;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSite() {
        return site;
    }

    public void setSite(String site) {
        this.site = site;
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

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public int getImageWidth() {
        return imageWidth;
    }

    public void setImageWidth(int imageWidth) {
        this.imageWidth = imageWidth;
    }

    public int getImageHeight() {
        return imageHeight;
    }

    public void setImageHeight(int imageHeight) {
        this.imageHeight = imageHeight;
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

    public long getImageSize() {
        return imageSize;
    }

    public void setImageSize(long imageSize) {
        this.imageSize = imageSize;
    }

}
