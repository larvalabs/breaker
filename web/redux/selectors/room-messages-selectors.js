import { createSelector } from 'reselect';
import Immutable from 'immutable';

const getCurrentRoomName = (state) => state.get('currentRoom');
const getRoomMessages = (state) => state.get('roomMessages');
const getMessages = (state) => state.get('messages');

export const getMessagesForCurrentRoom = createSelector(
    [getCurrentRoomName, getRoomMessages],
    (currentRoomName, roomMessages) => {
      return roomMessages.get(currentRoomName, Immutable.List());
    }
);

export const getFirstMessageForCurrentRoom = createSelector(
    [getMessagesForCurrentRoom, getMessages],
    (currentRoomMessages, messages) => {
      return messages.get(currentRoomMessages.first(), Immutable.Map());
    }
);

export const getCurrentRoomHasMoreMessages = createSelector(
    [getFirstMessageForCurrentRoom],
    (firstMessage) => {
      return firstMessage.get('type', '') !== 'first_sentinel';
    }
);

export const getMessageCountCurrentRoom = createSelector(
    [getMessagesForCurrentRoom],
    (currentRoomMessages) => {
      return currentRoomMessages.size;
    }
);
