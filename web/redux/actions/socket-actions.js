import socket from '../../socket';
import API from '../../api';

import * as actions from '../constants/socket-constants';
import * as chatActions from './chat-actions';
import * as notifyActions from './notification-actions';


const MESSAGE_TYPE_ROOM_LIST = 'roomlist';
const MESSAGE_TYPE_MESSAGE = 'message';
const MESSAGE_TYPE_SERVER = 'servermessage';
const MESSAGE_TYPE_JOIN = 'join';
const MESSAGE_TYPE_LEAVE = 'leave';
const MESSAGE_TYPE_ROOMLEAVE = 'roomleave';
const MESSAGE_TYPE_USERLEAVE = 'userleave';
const MESSAGE_TYPE_MEMBERS = 'memberlist';
const MESSAGE_TYPE_UPDATE_USER = 'updateuser';
const MESSAGE_TYPE_UPDATE_ROOM = 'updateroom';
const MESSAGE_TYPE_UPDATE_MESSAGE = 'updatemessage';
const MESSAGE_TYPE_UPDATE_LAST_READ = 'markedread';

export function onSocketMessage(message) {
  return (dispatch, getStore) => {
    switch (message.type) {
      case MESSAGE_TYPE_ROOM_LIST: {
        return dispatch({ type: actions.SOCK_ROOM_LIST, message });
      }
      case MESSAGE_TYPE_MESSAGE: {
        const store = getStore();
        const hasFocus = store.getIn(['ui', '__HAS_FOCUS__'], false);
        const currentRoom = store.get('currentRoom');
        if (hasFocus && currentRoom === message.room.name) {
          socket().sendRoomMessagesSeen(currentRoom);

          const shapedMessage = { lastReadTime: message.message.createDateLongUTC, room: message.room };
          dispatch({ type: actions.SOCK_UPDATE_LAST_READ, message: shapedMessage });
        }

        dispatch(notifyActions.handleNewMessageNotification(message));
        return dispatch({ type: actions.SOCK_MESSAGE, message });
      }
      case MESSAGE_TYPE_SERVER: {
        return dispatch({ type: actions.SOCK_SERVER, message });
      }
      case MESSAGE_TYPE_JOIN: {
        return dispatch({ type: actions.SOCK_JOIN, message });
      }
      case MESSAGE_TYPE_LEAVE: {
        return dispatch({ type: actions.SOCK_LEAVE, message });
      }
      case MESSAGE_TYPE_ROOMLEAVE: {
        const store = getStore();
        const removedRoomName = message.room.name;

        let selectedRoom = store.get('rooms').first().get('name');
        store.get('rooms').reduce((prev, nextValue, nextKey) => {
          if (prev === removedRoomName) {
            selectedRoom = nextKey;
          }

          return nextKey;
        }, '');

        dispatch(chatActions.handleChangeRoom(selectedRoom));
        return dispatch({ type: actions.SOCK_ROOMLEAVE, message });
      }
      case MESSAGE_TYPE_USERLEAVE: {
        return dispatch({ type: actions.SOCK_USERLEAVE, message });
      }
      case MESSAGE_TYPE_MEMBERS: {
        return dispatch({ type: actions.SOCK_MEMBERS, message });
      }
      case MESSAGE_TYPE_UPDATE_USER: {
        return dispatch({ type: actions.SOCK_UPDATE_USER, message });
      }
      case MESSAGE_TYPE_UPDATE_ROOM: {
        return dispatch({ type: actions.SOCK_UPDATE_ROOM, message });
      }
      case MESSAGE_TYPE_UPDATE_MESSAGE: {
        return dispatch({ type: actions.SOCK_UPDATE_MESSAGE, message });
      }
      case MESSAGE_TYPE_UPDATE_LAST_READ: {
        return dispatch({ type: actions.SOCK_UPDATE_LAST_READ, message });
      }
      default: {
        return dispatch({ type: actions.SOCK_UNKNOWN, message });
      }
    }
  };
}

export function onSocketOpen() {
  return { type: actions.SOCK_OPEN };
}

export function onSocketClose() {
  return { type: actions.SOCK_CLOSE };
}

export function handleSocketOpen(store, socketInstance) {
  return dispatch => {
    const currentRoom = store.getState().get('currentRoom');
    socketInstance.startRoomPing(currentRoom);

    dispatch(onSocketOpen());
  };
}

export function handleSocketClose(store, socketInstance) {
  return dispatch => {
    const currentRoom = store.getState().get('currentRoom');
    socketInstance.stopRoomPing(currentRoom);

    dispatch(onSocketClose());
  };
}

export function stateRefreshed(newState) {
  return { type: actions.SOCK_REFRESH, state: newState };
}

export function handleStateRefresh() {
  return dispatch => {
    API.requestInitialState().then((data) => dispatch(stateRefreshed(data.data)));
  };
}
