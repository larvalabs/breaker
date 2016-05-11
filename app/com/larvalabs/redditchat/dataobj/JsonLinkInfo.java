package com.larvalabs.redditchat.dataobj;

import models.WebLink;

/**
 * Created by matt on 5/11/16.
 */
public class JsonLinkInfo {

    private String uuid;
    private String url;
    private String type;
    private String breakerType;
    private String site;
    private String title;
    private String description;
    private String imageUrl;
    private Integer imageWidth;
    private Integer imageHeight;
    private Long imageSize;
    private String videoUrl;
    private String videoType;
    private Integer videoWidth;
    private Integer videoHeight;

    public JsonLinkInfo() {
    }

    public static JsonLinkInfo from(WebLink webLink) {
        JsonLinkInfo info = new JsonLinkInfo();
        info.setUuid(webLink.getUuid());
        info.setUrl(webLink.getUrl());
        info.setType(webLink.getType());
        info.setSite(webLink.getSite());
        info.setTitle(webLink.getTitle());
        info.setDescription(webLink.getDescription());
        info.setImageUrl(webLink.getImageUrl());
        if (webLink.getImageWidth() > 0) {
            info.setImageWidth(webLink.getImageWidth());
        }
        if (webLink.getImageHeight() > 0) {
            info.setImageHeight(webLink.getImageHeight());
        }
        if (webLink.getImageSize() > 0) {
            info.setImageSize(webLink.getImageSize());
        }
        info.setVideoUrl(webLink.getVideoUrl());
        if (webLink.getVideoWidth() > 0) {
            info.setVideoWidth(webLink.getVideoWidth());
        }
        if (webLink.getVideoHeight() > 0) {
            info.setVideoHeight(webLink.getVideoHeight());
        }
        // Set breaker type
        if (info.getType() != null && info.getType().equals("image")) {
            info.setBreakerType("image");
        } else if (info.getType() != null && info.getType().toLowerCase().contains("video")) {
            info.setBreakerType("video");
        } else if (info.getVideoUrl() != null) {
            info.setBreakerType("video");
        } else {
            info.setBreakerType("link");
        }
        return info;
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

    public String getBreakerType() {
        return breakerType;
    }

    public void setBreakerType(String breakerType) {
        this.breakerType = breakerType;
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

    public Integer getImageWidth() {
        return imageWidth;
    }

    public void setImageWidth(Integer imageWidth) {
        this.imageWidth = imageWidth;
    }

    public Integer getImageHeight() {
        return imageHeight;
    }

    public void setImageHeight(Integer imageHeight) {
        this.imageHeight = imageHeight;
    }

    public Long getImageSize() {
        return imageSize;
    }

    public void setImageSize(Long imageSize) {
        this.imageSize = imageSize;
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

    public Integer getVideoWidth() {
        return videoWidth;
    }

    public void setVideoWidth(Integer videoWidth) {
        this.videoWidth = videoWidth;
    }

    public Integer getVideoHeight() {
        return videoHeight;
    }

    public void setVideoHeight(Integer videoHeight) {
        this.videoHeight = videoHeight;
    }
}
