import socket from '../../socket';

import * as actions from '../constants/chat-constants';
import * as menuActions from './menu-actions';


function collapseLink(messageId) {
  return { type: actions.CHAT_LINK_COLLAPSED, messageId };
}

function expandLink(messageId) {
  return { type: actions.CHAT_LINK_EXPANDED, messageId };
}

export function toggleCollapseLink(uuid) {
  return (dispatch, getState) => {
    const isItemCollapsed = !!getState().getIn(['ui', 'collapsedLinks', uuid]);
    if (isItemCollapsed) {
      return dispatch(expandLink(uuid));
    }

    return dispatch(collapseLink(uuid));
  };
}

export function chatMessageSent(messageObj) {
  return { type: actions.CHAT_MESSAGE_SENT, messageObj };
}

export function sendNewMessage(messageObj) {
  return dispatch => {
    socket().send(JSON.stringify(messageObj));
    dispatch(chatMessageSent(messageObj));
  };
}

export function setChatInputFocus() {
  return { type: actions.CHAT_SET_INPUT_FOCUS };
}

export function resetChatInputFocus() {
  return { type: actions.CHAT_RESET_INPUT_FOCUS };
}

export function chatBlurred(roomName) {
  return { type: actions.CHAT_BLURRED, roomName };
}

export function chatFocused(roomName) {
  socket().sendRoomMessagesSeen(roomName);
  return { type: actions.CHAT_FOCUSED, roomName };
}

function changeRoom(roomName, lastMessageTime) {
  return { type: actions.CHAT_ROOM_CHANGED, roomName, lastMessageTime };
}

export function handleChangeRoom(roomName) {
  return (dispatch, getState) => {
    socket().sendRoomMessagesSeen(roomName);

    window.history.replaceState({}, 'Breaker: ', `/r/${roomName}`);

    const state = getState();
    const lastMessageTime = state.getIn(
        ['messages', state.getIn(['roomMessages', roomName]).last(), 'createDateLongUTC']
    );
    dispatch(changeRoom(roomName, lastMessageTime));
    dispatch(menuActions.handleCloseAllMenus());

    if (window.innerWidth > 450) {
      // Don't do this on mobile
      dispatch(setChatInputFocus());
    }
  };
}

export function loadingMoreMessages() {
  return { type: actions.CHAT_LOADING_MESSAGES };
}

export function loadedMoreMessages(room, messages) {
  return { type: actions.CHAT_LOADED_MESSAGES, room, messages };
}

export function failedLoadingMoreMessages(error) {
  return { type: actions.CHAT_FAILED_LOADING_MESSAGES, error };
}
