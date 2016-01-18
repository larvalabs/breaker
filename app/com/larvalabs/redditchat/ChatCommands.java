package com.larvalabs.redditchat;

import com.larvalabs.redditchat.dataobj.JsonChatRoom;
import com.larvalabs.redditchat.realtime.ChatRoomStream;
import controllers.WebSocket;
import models.ChatRoom;
import models.ChatUser;
import models.ChatUserRoomJoin;
import org.apache.commons.lang.StringUtils;
import play.Logger;
import play.mvc.Http;

/**
 * Created by matt on 1/18/16.
 */
public class ChatCommands {

    public static final String COMMAND_CHAR = "/";

    public enum CommandType {
        KICK("kick"),
        BAN("ban"),
        UNBAN("unban");

        String commandString;

        CommandType(String commandString) {
            this.commandString = commandString;
        }

        public String getCommandString() {
            return commandString;
        }

        public static CommandType getType(String message) {
            for (CommandType commandType : values()) {
                if (message.toLowerCase().startsWith(COMMAND_CHAR + commandType.getCommandString())) {
                    return commandType;
                }
            }
            return null;
        }

    }

    public static class Command {
        public CommandType type;
        public String username;

        public Command(CommandType type, String username) {
            this.type = type;
            this.username = username;
        }
    }

    public static boolean isCommand(String message) {
        if (!StringUtils.isEmpty(message)) {
            if (message.trim().startsWith(COMMAND_CHAR)) {
                return true;
            }
        }
        return false;
    }

    private static String getUsername(String message) {
        if (!StringUtils.isEmpty(message)) {
            String[] split = message.split(" ");
            if (split.length > 1) {
                String username = split[1];
                username = username.replace("@", "");
                return username;
            }
        }
        return null;
    }

    public static Command getCommand(String message) {
        if (!StringUtils.isEmpty(message)) {
            message = message.trim();
            if (message.startsWith(COMMAND_CHAR)) {
                CommandType type = CommandType.getType(message);
                if (type != null) {
                    String username = getUsername(message);
                    if (username != null) {
                        return new Command(type, username);
                    }
                }
            }
        }
        return null;
    }

    public static void execCommand(ChatRoom room, String message, ChatRoomStream stream, Http.Outbound socket) {
        if (!isCommand(message) || getCommand(message) == null) {
            Logger.debug("Error processing message, notifying user.");
            socket.send(new ChatRoomStream.ServerMessage(JsonChatRoom.from(room), "Error processing command.").toJson());
            return;
        }

        Command command = getCommand(message);
        if (command.type == CommandType.BAN || command.type == CommandType.UNBAN) {
            ChatUser user = ChatUser.findByUsername(command.username);
            if (user != null) {
                // Need to load new copies of the objects to be able to save via JPA
                ChatRoom loadedRoom = ChatRoom.findByName(room.name);
                ChatUser loadedUser = ChatUser.findByUsername(command.username);
                loadedRoom.getBannedUsers().add(loadedUser);
                loadedRoom.save();

                ChatUserRoomJoin join = ChatUserRoomJoin.findByUserAndRoom(user, room);
                join.delete();

                if (command.type == CommandType.BAN) {
                    socket.send(new ChatRoomStream.ServerMessage(JsonChatRoom.from(room), "User " + command.username + " has been banned.").toJson());
                } else {
                    socket.send(new ChatRoomStream.ServerMessage(JsonChatRoom.from(room), "User " + command.username + " is now unbanned.").toJson());
                }
            } else {
                socket.send(new ChatRoomStream.ServerMessage(JsonChatRoom.from(room), "User " + command.username + " was not found.").toJson());
            }
        } else if (command.type == CommandType.KICK) {
            socket.send(new ChatRoomStream.ServerMessage(JsonChatRoom.from(room), "User " + command.username + " kicked.").toJson());
            stream.publishEvent(new ChatRoomStream.ServerCommand(JsonChatRoom.from(room), command), true);
        }
    }
}
