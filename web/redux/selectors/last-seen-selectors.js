import { createSelector } from 'reselect';

import Config from '../../config';


const getLastSeenTimes = (state) => state.get('lastSeenTimes');
const getLastSeenTimeForRoom = (state, props) => state.getIn(['lastSeenTimes', props.room.get('name')]);
const getRoomMessagesForRoom = (state, props) => state.getIn(['roomMessages', props.room.get('name')]);
const getRoomMessages = (state) => state.get('roomMessages');
const getMessages = (state) => state.get('messages');
const getCurrentRoomName = (state) => state.get('currentRoom');

export const getTotalLastSeenTimes = createSelector(
    [getLastSeenTimes, getRoomMessages, getMessages, getCurrentRoomName],
    (lastSeenTimes, roomMessages, messages, currentRoomName) => {
      let filteredLastSeenTimes = lastSeenTimes;

      // TODO: this should be in the state
      if (Config.guest) {
        filteredLastSeenTimes = lastSeenTimes.filter((_, currRoomName) => currRoomName === currentRoomName);
      }

      return filteredLastSeenTimes.reduce((total, lastReadTime, currRoomName) => {
        return total + roomMessages.get(currRoomName).reduce((innerTotal, messageId) => {
          const messageTime = messages.getIn([messageId, 'createDateLongUTC']);
          return messageTime && messageTime - lastReadTime > 0 ? innerTotal + 1 : innerTotal;
        }, 0);
      }, 0);
    }
);

export const makeGetLastSeenTimeForRoom = () => {
  return createSelector(
    [getLastSeenTimeForRoom, getRoomMessagesForRoom, getMessages],
    (lastSeenTimeForRoom, roomMessagesForRoom, messages) => {
      return roomMessagesForRoom.reduce((total, messageId) => {
        const messageTime = messages.getIn([messageId, 'createDateLongUTC']);
        return messageTime && messageTime - lastSeenTimeForRoom > 0 ? total + 1 : total;
      }, 0);
    }
  );
};
