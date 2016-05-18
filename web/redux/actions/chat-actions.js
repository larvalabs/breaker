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
    let currentRoom = state.get('currentRoom');
    let firstMessage = state.getIn(
        ['messages', state.getIn(['roomMessages', currentRoom], Immutable.List()).first()
    ]);
    dispatch(loadingMoreMessages());
    API.fetchMoreMessages(currentRoom, firstMessage.get('id')).then((response) => {
      debugger;
      dispatch(loadedMoreMessages(currentRoom, response.data))
    }).catch((error) => {
      dispatch(failedLoadingMoreMessages(error))
    })
  }
}


var NEW_MESSAGES = ["837cb2ab-3a98-4de5-809a-29f3101918e8", "6a6ce45d-30d7-45b5-98ce-f8b2b124c4c5", "03ecb2b1-717c-40f7-aa89-e1bf99a15c56", "52273d12-e05f-47e0-bee4-3d452435fb03", "ca20595f-bbd8-41a4-bd2c-19f6c8f6352f", "75a79442-dcdb-4479-944a-d3bc450b9587", "b1b009ff-c493-4b22-92c4-faf3b578c09e", "af7d49ee-7330-4ca8-b4e6-575894405eaf", "cafc5d90-c5b0-4172-ba99-debe7936a2d5", "fb145fb1-2e95-4981-b69a-de1d9a98993d", "3273fbe2-8a7f-4f96-94ee-e5b116b63a93", "154338c0-c292-4f6e-83df-6761e13e41aa", "a3b7276a-c439-4080-857a-62bf45307c3b", "6dd64841-1bc3-472a-aa39-5b86721038db", "c42d274f-69ef-4daf-a54f-b0a28b0c87a2", "695fbc04-35f5-4ac9-b4c0-176beb3d4abc", "36125442-7e04-44f9-a7ff-3987419ea4d2", "59c26335-d5cb-473f-ad88-59a42916867f", "1bde9ff9-9081-4588-bce5-bc0818700dbb", "2faff7eb-b71a-4bb6-a5cc-4ed20fa1cd9e"]
