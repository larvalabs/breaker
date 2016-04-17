import * as actions from '../constants/chat-constants.js';
import socket from '../../socket.js';

export function chatMessageSent(messageObj) {
  return { type: actions.CHAT_MESSAGE_SENT, messageObj };
}

export function sendNewMessage(messageObj) {
  return dispatch => {
    socket().send(JSON.stringify(messageObj));
    dispatch(chatMessageSent(messageObj))
  }
}
