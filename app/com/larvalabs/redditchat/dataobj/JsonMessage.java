package com.larvalabs.redditchat.dataobj;

import models.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class JsonMessage {

    public long id;
    public String uuid;
    public JsonUser user;
    public String message;
    public String imageUrl;
    public String imageThumbUrl;
    public int likeCount;
    public boolean userDidLike;
    public Date createDate;
    public long createDateLongUTC;
    public boolean isNewSinceLastSession;
    public String detectedLanguage;

    // If true means it was sent before saving, so doesn't contain all info
    public boolean partial = false;

    // Optionally filled when showing list of messages loaded from various rooms
    public JsonChatRoom room;

    // Optionally filled if translation exists
//    public String translatedMessage;
//    public String translatedLanguage;

    public JsonMessage(long id, String uuid, JsonUser user, String message, String imageUrl, String imageThumbUrl, int likeCount, boolean userDidLike,
                       Date createDate, long createDateLongUTC, boolean newSinceLastSession, String detectedLanguage) {
        this.id = id;
        this.uuid = uuid;
        this.user = user;
        this.message = message;
        this.imageUrl = imageUrl;
        this.imageThumbUrl = imageThumbUrl;
        this.likeCount = likeCount;
        this.userDidLike = userDidLike;
        this.createDate = createDate;
        this.createDateLongUTC = createDateLongUTC;
        isNewSinceLastSession = newSinceLastSession;
        this.detectedLanguage = detectedLanguage;
    }

    public JsonMessage(String uuid, JsonUser user, JsonChatRoom room, String message) {
        this.uuid = uuid;
        this.user = user;
        this.room = room;
        this.message = message;
    }

    public static JsonMessage from(Message message, ChatUser loggedInUser, boolean isNewSinceLastSession) {
        return from(message, loggedInUser, isNewSinceLastSession, null);
    }

    public static JsonMessage from(Message message, ChatUser loggedInUser, boolean isNewSinceLastSession, List<Message> likedMessages) {
        boolean didLikeMessage = false;
        if (likedMessages != null) {
            didLikeMessage = likedMessages.contains(message);
        } else {
            didLikeMessage = message.didUserLike(loggedInUser);
        }
        return new JsonMessage(message.getId(), message.getUuid(), JsonUser.fromUser(message.user),
                message.messageText, message.getImageUrl(), message.getImageThumbUrl(),
                message.getLikeCount(), didLikeMessage, message.createDate, message.createDate.getTime(),
                isNewSinceLastSession, message.getLanguageDetected());
    }

    public static JsonMessage makePresavedMessage(String uuid, ChatUser user, ChatRoom room, String message) {
        return new JsonMessage(uuid, JsonUser.fromUser(user), JsonChatRoom.from(room, user, null, false), message);
    }

    public enum ListType {
        RECENT_MESSAGES,
        MENTIONS;
    }

    /**
     * Note: Ignores new status of messages
     * @param messages
     * @param loggedInUser
     * @param addChatRoomReferences Load the details of the chat room for each message. Slower, but used when the messages all come from different rooms.
     * @return
     */
    public static JsonMessage[] convert(List<Message> messages, ChatUser loggedInUser, ListType type) {
        ArrayList<JsonMessage> jsonMessages = new ArrayList<JsonMessage>();
        for (Message message : messages) {
            boolean isNew = false;
            if (type == ListType.MENTIONS) {
                isNew = message.getId() > loggedInUser.getLastSeenMentionedMessageId();
            }
            JsonMessage jsonMessage = JsonMessage.from(message, loggedInUser, isNew);
            if (type == ListType.RECENT_MESSAGES || type == ListType.MENTIONS) {
                jsonMessage.room = JsonChatRoom.from(message.getRoom(), loggedInUser, null, false);
            }
            jsonMessages.add(jsonMessage);
        }
        return jsonMessages.toArray(new JsonMessage[]{});
    }

/*
    public void fillTranslation(Translation translation) {
        translatedMessage = translation.getTranslatedMessageText();
        translatedLanguage = translation.getLanguage();
    }
*/
}
