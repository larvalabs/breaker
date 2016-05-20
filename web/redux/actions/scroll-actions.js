import * as actions from '../constants/scroll-constants'
import * as chatActions from '../actions/chat-actions'
import {API} from '../../api'
import Immutable from 'immutable'

export function scrollToMessage(messageId) {
  return { type: actions.SCROLL_TO_MESSAGE_ID, messageId};
}

export function resetScrollToMessage() {
  return { type: actions.SCROLL_TO_MESSAGE_ID_RESET};
}

export function handleMoreMessages() {
  return (dispatch, getState) => {
    let state = getState();
    let count = 20;
    let currentRoom = state.get('currentRoom');
    let firstMessage = state.getIn(
        ['messages', state.getIn(['roomMessages', currentRoom], Immutable.List()).first()
        ]);
    if(firstMessage && firstMessage.get('type') !== 'first_sentinel') {
      dispatch(chatActions.loadingMoreMessages());
      API.fetchMoreMessages(currentRoom, firstMessage.get('id')).then((response) => {
        if (response.data.length < count) {
          response.data.push({
            "uuid": currentRoom + "_first_sentinel",
            "type": "first_sentinel"
          })
        }

        dispatch(scrollToMessage(firstMessage.get('uuid')));
        dispatch(chatActions.loadedMoreMessages(currentRoom, response.data));
      }).catch((error) => {
        dispatch(chatActions.failedLoadingMoreMessages(error))
      })
    }
  }
}

export function scrollToRoomName(roomName) {
  return { type: actions.SCROLL_TO_ROOM_NAME, roomName};
}

export function scrollToRoomNameReset() {
  return { type: actions.SCROLL_TO_ROOM_NAME_RESET};
}

export function handleScrollToNextUnread() {
  return (dispatch, getState) => {
    let state = getState();
    let lastReadTimes = state.get('lastSeenTimes');
    let firstUnreadRoomName = lastReadTimes.findKey((lastReadTime, roomName) => {
      let unreadCountForRoom = state.getIn(['roomMessages', roomName]).reduce((total, messageId) => {
        let messageTime = state.getIn(['messages', messageId, 'createDateLongUTC']);
        return messageTime && messageTime - lastReadTime > 0 ? total + 1 : total;
      }, 0);
      return unreadCountForRoom > 0
    });
    if(firstUnreadRoomName) {
      dispatch(chatActions.handleChangeRoom(firstUnreadRoomName));
      dispatch(scrollToRoomName(firstUnreadRoomName));
    }
  }
}
