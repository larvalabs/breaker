package com.larvalabs.redditchat.dataobj;

import com.larvalabs.linkunfurl.LinkInfo;
import models.*;
import org.nibor.autolink.*;
import play.Logger;

import java.io.Serializable;
import java.util.*;

public class JsonMessage implements Serializable {

    public long id;
    public String uuid;
    public String username;
    public String message;
    public String imageUrl;
    public String imageThumbUrl;
    public int likeCount;
    public boolean userDidLike;
    public Date createDate;
    public long createDateLongUTC;
    public boolean isNewSinceLastSession;
    public String detectedLanguage;

    public String messageHtml;
    public String[] allLinks;
    public String[] imageLinks;

    public String[] mentionedUsernames;

    public LinkInfo[] linkInfo;

    // If true means it was sent before saving, so doesn't contain all info
    public boolean partial = false;

    // Optionally filled when showing list of messages loaded from various rooms
    public String roomName;

    // Optionally filled if translation exists
//    public String translatedMessage;
//    public String translatedLanguage;

    public JsonMessage(long id, String uuid, String username, String roomName, String message, String imageUrl, String imageThumbUrl, int likeCount, boolean userDidLike,
                       Date createDate, long createDateLongUTC, boolean newSinceLastSession, String detectedLanguage) {
        this.id = id;
        this.uuid = uuid;
        this.username = username;
        this.roomName = roomName;
        this.message = message;
        this.imageUrl = imageUrl;
        this.imageThumbUrl = imageThumbUrl;
        this.likeCount = likeCount;
        this.userDidLike = userDidLike;
        this.createDate = createDate;
        this.createDateLongUTC = createDateLongUTC;
        isNewSinceLastSession = newSinceLastSession;
        this.detectedLanguage = detectedLanguage;
        try {
            processMessage();
        } catch (Exception e) {
            Logger.error(e, "Error extracting links from message text.");
        }
    }

    public JsonMessage(String uuid, String username, String roomName, String message) {
        this.uuid = uuid;
        this.username = username;
        this.roomName = roomName;
        this.message = message;
        this.createDate = new Date();
        this.createDateLongUTC = createDate.getTime();
        try {
            processMessage();
        } catch (Exception e) {
            Logger.error(e, "Error extracting links from message text.");
        }

    }

    public static JsonMessage from(Message message, String username, String roomName) {
        return new JsonMessage(message.getId(), message.getUuid(), username, roomName,
                message.messageText, message.getImageUrl(), message.getImageThumbUrl(),
                message.getLikeCount(), false, message.createDate, message.createDate.getTime(),
                false, message.getLanguageDetected());
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
        return new JsonMessage(message.getId(), message.getUuid(), message.getUser().getUsername(), message.getRoom().getName(),
                message.messageText, message.getImageUrl(), message.getImageThumbUrl(),
                message.getLikeCount(), didLikeMessage, message.createDate, message.createDate.getTime(),
                isNewSinceLastSession, message.getLanguageDetected());
    }

    public static JsonMessage makePresavedMessage(String uuid, String username, String roomName, String message) {
        return new JsonMessage(uuid, username, roomName, message);
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
                jsonMessage.roomName = message.getRoom().getName();
            }
            jsonMessages.add(jsonMessage);
        }
        return jsonMessages.toArray(new JsonMessage[]{});
    }

    public void processMessage() throws Exception {
        LinkExtractor linkExtractor = LinkExtractor.builder()
                .linkTypes(EnumSet.of(LinkType.URL)) // limit to URLs
                .build();

        final ArrayList<String> localLinks = new ArrayList<String>();
        final ArrayList<String> localImageLinks = new ArrayList<String>();

        Iterable<LinkSpan> links = linkExtractor.extractLinks(message);
        messageHtml = Autolink.renderLinks(message, links,
                new LinkRenderer() {
                    @Override
                    public void render(LinkSpan link, CharSequence text, StringBuilder sb) {
                        String linkStr = text.toString().substring(link.getBeginIndex(), link.getEndIndex());
                        localLinks.add(linkStr);
                        // todo this is budget
                        String linkStrLower = linkStr.toLowerCase();
                        if (linkStrLower.endsWith(".jpg") || linkStrLower.endsWith(".jpeg") ||
                                linkStrLower.endsWith(".png") || linkStrLower.endsWith(".gif")) {
                            localImageLinks.add(linkStr);
                        }
                        sb.append("<a class=\"text-info\" href=\"");
                        sb.append(text, link.getBeginIndex(), link.getEndIndex());
                        sb.append("\" target='_blank'>");
                        sb.append(text, link.getBeginIndex(), link.getEndIndex());
                        sb.append("</a>");
                    }
                });

        if (localLinks.size() > 0) {
            allLinks = localLinks.toArray(new String[]{});
        }
        if (localImageLinks.size() > 0) {
            imageLinks = localImageLinks.toArray(new String[]{});
        }

        mentionedUsernames = Message.getMentionedUsernames(message).toArray(new String[]{});
    }

    public void setLinkInfo(Set<WebLink> webLinks) {
        ArrayList<LinkInfo> linkInfos = new ArrayList<>();
        for (WebLink webLink : webLinks) {
            linkInfos.add(webLink.getLinkInfo());
        }
        linkInfo = linkInfos.toArray(new LinkInfo[]{});
    }

/*
    public void fillTranslation(Translation translation) {
        translatedMessage = translation.getTranslatedMessageText();
        translatedLanguage = translation.getLanguage();
    }
*/
}
