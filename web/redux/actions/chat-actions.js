import * as actions from '../constants/chat-constants';
import * as menuActions from './menu-actions';
import socket from '../../socket';
import {API} from '../../api'
import Immutable from 'immutable'

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

export function setChatInputFocus() {
  return { type: actions.CHAT_SET_INPUT_FOCUS};
}

export function resetChatInputFocus() {
  return { type: actions.CHAT_RESET_INPUT_FOCUS};
}

export function scrollToMessage(messageId) {
  return { type: actions.CHAT_SCROLL_TO_MESSAGE, messageId};
}

export function resetScrollToMessage() {
  return { type: actions.CHAT_RESET_INPUT_FOCUS};
}

export function chatBlurred(roomName) {
  return { type: actions.CHAT_BLURRED, roomName };
}

export function chatFocused(roomName) {
  socket().sendRoomMessagesSeen(roomName);
  return { type: actions.CHAT_FOCUSED, roomName };
}

function changeRoom(roomName, lastMessageTime) {
  return {type: actions.CHAT_ROOM_CHANGED, roomName, lastMessageTime}
}

export function handleChangeRoom(roomName) {
  return (dispatch, getState) => {
    socket().sendRoomMessagesSeen(roomName);
    
    window.history.replaceState({}, 'Breaker: ', '/r/' + roomName);
    
    let state = getState();
    let lastMessageTime = state.getIn(['messages', state.getIn(['roomMessages', roomName]).last(), 'createDateLongUTC']);
    dispatch(changeRoom(roomName, lastMessageTime));
    dispatch(menuActions.sidebarClose());
    dispatch(setChatInputFocus())
  }
}

function loadingMoreMessages(){
  return {type: actions.CHAT_LOADING_MESSAGES}
}

function loadedMoreMessages(room, messages){
  return {type: actions.CHAT_LOADED_MESSAGES, room, messages}
}

function failedLoadingMoreMessages(error){
  return {type: actions.CHAT_FAILED_LOADING_MESSAGES, error}
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
      dispatch(loadingMoreMessages());
      API.fetchMoreMessages(currentRoom, firstMessage.get('id')).then((response) => {
        if (response.data.length < count) {
          response.data.push({
            "uuid": currentRoom + "_first_sentinel",
            "type": "first_sentinel"
          })
        }

        dispatch(scrollToMessage(firstMessage.get('uuid')));
        dispatch(loadedMoreMessages(currentRoom, response.data));
      }).catch((error) => {
        dispatch(failedLoadingMoreMessages(error))
      })
    }
  }
}
