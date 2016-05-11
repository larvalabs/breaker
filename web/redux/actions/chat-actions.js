import * as actions from '../constants/chat-constants';
import socket from '../../socket';

function collapseLink(messageId){
  return { type: actions.CHAT_LINK_COLLAPSED, messageId };
}

function expandLink(messageId){
  return { type: actions.CHAT_LINK_EXPANDED, messageId };
}

export function toggleCollapseLink(uuid) {
  return (dispatch, getState) => {
    let isItemCollapsed = !!getState().getIn(['ui', 'collapsedLinks', uuid]);
    if(isItemCollapsed){
      return dispatch(expandLink(uuid))
    } else {
      return dispatch(collapseLink(uuid))
    }

  }
}

export function chatMessageSent(messageObj) {
  return { type: actions.CHAT_MESSAGE_SENT, messageObj };
}

export function sendNewMessage(messageObj) {
  return dispatch => {
    socket().send(JSON.stringify(messageObj));
    dispatch(chatMessageSent(messageObj))
  }
}

export function chatBlurred(roomName) {
  return { type: actions.CHAT_BLURRED, roomName };
}

export function chatFocused(roomName) {
  return { type: actions.CHAT_FOCUSED, roomName };
}

function changeRoom(roomName) {
  return {type: actions.CHAT_ROOM_CHANGED, roomName}
}

export function handleChangeRoom(roomName) {
  return dispatch => {
    socket().sendRoomMessagesSeen(roomName);
    window.history.replaceState({}, 'Breaker: ', '/r/' + roomName);
    dispatch(changeRoom(roomName))
  }
}
