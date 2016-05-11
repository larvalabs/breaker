import * as actions from '../constants/socket-constants';

const MESSAGE_TYPE_ROOM_LIST = "roomlist";
const MESSAGE_TYPE_MESSAGE = "message";
const MESSAGE_TYPE_SERVER = "servermessage";
const MESSAGE_TYPE_JOIN = "join";
const MESSAGE_TYPE_LEAVE = "leave";
const MESSAGE_TYPE_MEMBERS = "memberlist";
const MESSAGE_TYPE_UPDATE_USER = "updateuser";
const MESSAGE_TYPE_UPDATE_ROOM = "updateroom";
const MESSAGE_TYPE_UPDATE_MESSAGE = "updatemessage";

export function onSocketMessage(message) {
  switch(message.type){
    case MESSAGE_TYPE_ROOM_LIST:
      return { type: actions.SOCK_ROOM_LIST, message };
    case MESSAGE_TYPE_MESSAGE:
      return { type: actions.SOCK_MESSAGE, message};
    case MESSAGE_TYPE_SERVER:
      return { type: actions.SOCK_SERVER, message};
    case MESSAGE_TYPE_JOIN:
      return { type: actions.SOCK_JOIN, message};
    case MESSAGE_TYPE_LEAVE:
      return { type: actions.SOCK_LEAVE, message};
    case MESSAGE_TYPE_MEMBERS:
      return { type: actions.SOCK_MEMBERS, message};
    case MESSAGE_TYPE_UPDATE_USER:
      return { type: actions.SOCK_UPDATE_USER, message};
    case MESSAGE_TYPE_UPDATE_ROOM:
      return { type: actions.SOCK_UPDATE_ROOM, message};
    case MESSAGE_TYPE_UPDATE_MESSAGE:
      return { type: actions.SOCK_UPDATE_MESSAGE, message};
    default:
      return { type: actions.SOCK_UNKNOWN, message };
  }
}


export function onSocketOpen() {
  return { type: actions.SOCK_OPEN };
}

export function onSocketClose() {
  return { type: actions.SOCK_CLOSE };
}

export function handleSocketOpen(store, socket) {
  return dispatch => {

    let currentRoom = store.getState().get('currentRoom');
    socket.startRoomPing(currentRoom);

    dispatch(onSocketOpen())
  }
}

export function handleSocketClose(store, socket) {
  return dispatch => {

    let currentRoom = store.getState().get('currentRoom');
    socket.stopRoomPing(currentRoom);
    
    dispatch(onSocketClose())
  }
}
