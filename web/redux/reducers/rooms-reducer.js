import Immutable from 'immutable';

import stateFromJS from '../../util/stateFromJS';

import * as socketTypes from '../constants/socket-constants';


export default function rooms(state = Immutable.Map(), action) {
  switch (action.type) {
    case (socketTypes.SOCK_MESSAGE):
    case (socketTypes.SOCK_MEMBERS): {
      return state.set(action.message.room.name, Immutable.fromJS(action.message.room));
    }
    case (socketTypes.SOCK_ROOMLEAVE): {
      return state.remove(action.message.room.name);
    }
    case (socketTypes.SOCK_ROOM_LIST): {
      let nextState = state;
      for (const room of action.message.rooms) {
        nextState = nextState.set(room.name, Immutable.fromJS(room));
      }
      return nextState;
    }
    case (socketTypes.SOCK_UPDATE_ROOM): {
      return state.set(action.message.room.name, Immutable.fromJS(action.message.room));
    }
    case (socketTypes.SOCK_REFRESH): {
      return stateFromJS(action.state.rooms);
    }
    default:
      return state;
  }
}
