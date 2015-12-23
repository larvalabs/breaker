package com.larvalabs.redditchat;

public class Constants {
    public enum Flair {
        DEV_SAME_ROOM(0x1F451),
        TOP_STARS_ROOM(0x1F31F),
        TOP_STARS_GLOBAL(0x1F3C6);

        int emojiCodePoint;

        private Flair(int emojiCodePoint) {
            this.emojiCodePoint = emojiCodePoint;
        }

        public String getAsString() {
            return new String(Character.toChars(emojiCodePoint));
        }
    }

    public static final String HEADER_X_REQUEST_ID = "x-request-id";
    public static final String HEADER_X_REQUEST_START = "x-request-start";

    public static final int NUMBER_DAYS_INACTIVE_STOP_NOTIFICATIONS = 21;

    public static final int CODE_LENGTH = 6;

    public static final int THRESHOLD_MESSAGE_FLAG = 5; // Number of flags before a message is hidden
    public static final int THRESHOLD_USER_FLAG = 15;   // Number of flags for a user before they're blocked
    public static final int THRESHOLD_ROOM_USERS_FOR_TOP_LIST = 14;   // Number of users required in a room for it to appear on top list

    public static final String URL_CLOUDFRONT_THUMB_PREFIX = "http://d1j13ers05ggmx.cloudfront.net/thumb?url=";
    public static final String URL_CLOUDINARY_FULLSIZE_PREFIX = "http://res.cloudinary.com/appchat/image/fetch/";
    public static final String URL_S3_BUCKET_SCREENSHOT_FULLSIZE = "http://appchat-screenshots.s3.amazonaws.com/";
    public static final String URL_S3_BUCKET_PROFILE_FULLSIZE = "http://appchat-userprofile.s3.amazonaws.com/";
    public static final String URL_S3_BUCKET_WALLPAPER_FULLSIZE = "http://appchat-wallpaper.s3.amazonaws.com/";
    public static final String URL_WALLPAPER_THUMB = URL_CLOUDFRONT_THUMB_PREFIX;

    public static final long TIMEOUT_TRANSLATE_SECONDS = 1;

    public static final String APPCHAT_PACKAGE_NAME = "com.larvalabs.myapps";

    public static final int PRESENCE_TIMEOUT_SEC = 30;

    public static final int USER_FLAG_THRESHOLD = 15;

    public static final int DEFAULT_MESSAGE_LIMIT = 40;
}
