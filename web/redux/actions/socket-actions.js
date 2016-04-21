import * as actions from '../constants/socket-constants';

const MESSAGE_TYPE_ROOM_LIST = "roomlist";
const MESSAGE_TYPE_MESSAGE = "message";
const MESSAGE_TYPE_SERVER = "servermessage";
const MESSAGE_TYPE_JOIN = "join";
const MESSAGE_TYPE_LEAVE = "leave";
const MESSAGE_TYPE_MEMBERS = "memberlist";

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
    default:
      return { type: actions.SOCK_UNKNOWN, message };
  }
}


function onSocketOpen() {
  return { type: actions.SOCK_OPEN };
}

function onSocketClose() {
  return { type: actions.SOCK_CLOSE };
}

export function handleSocketOpen(store, socket) {
  return dispatch => {

    let initialRoom = store.getState().getIn(['initial', 'roomName']);
    socket.startRoomPing(initialRoom);

    dispatch(onSocketOpen())
  }
}

export function handleSocketClose(store, socket) {
  return dispatch => {

    let initialRoom = store.getState().getIn(['initial', 'roomName']);
    socket.stopRoomPing(initialRoom);
    
    dispatch(onSocketClose())
  }
}
