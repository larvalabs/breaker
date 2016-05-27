import Immutable from 'immutable';

import API from '../../api';

import * as actions from '../constants/scroll-constants';
import * as chatActions from '../actions/chat-actions';


export function scrollToMessage(messageId) {
  return { type: actions.SCROLL_TO_MESSAGE_ID, messageId };
}

export function resetScrollToMessage() {
  return { type: actions.SCROLL_TO_MESSAGE_ID_RESET };
}

export function handleMoreMessages() {
  return (dispatch, getState) => {
    const state = getState();
    const count = 20;
    const currentRoom = state.get('currentRoom');
    const firstMessage = state.getIn(
        ['messages', state.getIn(['roomMessages', currentRoom], Immutable.List()).first()
        ]);
    if (firstMessage && firstMessage.get('type') !== 'first_sentinel') {
      dispatch(chatActions.loadingMoreMessages());
      API.fetchMoreMessages(currentRoom, firstMessage.get('id')).then((response) => {
        if (response.data.length < count) {
          response.data.push({
            uuid: `${currentRoom}_first_sentinel`,
            type: 'first_sentinel'
          });
        }

        dispatch(chatActions.loadedMoreMessages(currentRoom, response.data));
        dispatch(scrollToMessage(firstMessage.get('uuid')));
      }).catch((error) => {
        dispatch(chatActions.failedLoadingMoreMessages(error));
      });
    }
  };
}

export function scrollToRoomName(roomName) {
  return { type: actions.SCROLL_TO_ROOM_NAME, roomName };
}

export function scrollToRoomNameReset() {
  return { type: actions.SCROLL_TO_ROOM_NAME_RESET };
}

export function handleScrollToNextUnread() {
  return (dispatch, getState) => {
    const state = getState();
    const lastReadTimes = state.get('lastSeenTimes');
    const firstUnreadRoomName = lastReadTimes.findKey((lastReadTime, roomName) => {
      const unreadCountForRoom = state.getIn(['roomMessages', roomName]).reduce((total, messageId) => {
        const messageTime = state.getIn(['messages', messageId, 'createDateLongUTC']);
        return messageTime && messageTime - lastReadTime > 0 ? total + 1 : total;
      }, 0);
      return unreadCountForRoom > 0;
    });

    if (firstUnreadRoomName) {
      dispatch(chatActions.handleChangeRoom(firstUnreadRoomName));
      dispatch(scrollToRoomName(firstUnreadRoomName));
    }
  };
}
