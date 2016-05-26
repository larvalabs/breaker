import Immutable from 'immutable';

import stateFromJS from '../../util/stateFromJS';

import * as socketTypes from '../constants/socket-constants';
import * as chatTypes from '../constants/chat-constants';


export default function lastSeen(state = Immutable.Map(), action) {
  switch (action.type) {
    case (socketTypes.SOCK_UPDATE_LAST_READ): {
      return state.set(action.message.room.name, action.message.lastReadTime);
    }
    case (chatTypes.CHAT_ROOM_CHANGED): {
      return state.set(action.roomName, action.lastMessageTime);
    }
    case (socketTypes.SOCK_ROOMLEAVE): {
      return state.delete(action.message.room.name);
    }
    case (socketTypes.SOCK_REFRESH): {
      return stateFromJS(action.state.lastSeenTimes);
    }
    default: {
      return state;
    }
  }
}
