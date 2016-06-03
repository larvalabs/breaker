import { createSelector } from 'reselect'

import Config from '../../config';

const getLastSeenTimes = (state) => state.get('lastSeenTimes');
const getRoomMessages = (state) => state.get('roomMessages');
const getMessages = (state) => state.get('messages');
const getCurrentRoomName = (state) => state.get('currentRoom');

export const getTotalLastSeenTimes = createSelector(
    [getLastSeenTimes, getRoomMessages, getMessages, getCurrentRoomName],
    (lastSeenTimes, roomMessages, messages, currentRoomName) => {

      // TODO: this should be in the state
      if (Config.guest) {
        lastSeenTimes = lastSeenTimes.filter((_, currRoomName) => currRoomName === currentRoomName);
      }

      return lastSeenTimes.reduce((total, lastReadTime, currRoomName) => {
        return total + roomMessages.get(currRoomName).reduce((innerTotal, messageId) => {
              const messageTime = messages.getIn([messageId, 'createDateLongUTC']);
              return messageTime && messageTime - lastReadTime > 0 ? innerTotal + 1 : innerTotal;
            }, 0);
      }, 0);
    }
);
