import { createSelector } from 'reselect';

import { getMessagesForCurrentRoom } from './room-messages-selectors';


const getMessageEntities = (state) => state.get('messages');

export const getAllMessagesEntitiesForCurrentRoom = createSelector(
    [getMessagesForCurrentRoom, getMessageEntities],
    (roomMessages, messageEntities) => {
      return roomMessages.map((messageId) => messageEntities.get(messageId));
    }
);

export const getFirstMessageIdForCurrentRoom = createSelector(
    [getAllMessagesEntitiesForCurrentRoom],
    (messages) => {
      const firstMessage = messages.first();
      return firstMessage && firstMessage.get('uuid');
    }
);
