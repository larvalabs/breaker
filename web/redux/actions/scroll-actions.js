import * as actions from '../constants/scroll-constants'
import * as chatActions from '../actions/chat-actions'

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
