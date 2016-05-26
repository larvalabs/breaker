import Immutable from 'immutable';

import stateFromJS from '../../util/stateFromJS';

import * as socketTypes from '../constants/socket-constants';


export default function users(state = Immutable.Map(), action) {
  switch (action.type) {
    case (socketTypes.SOCK_LEAVE):
    case (socketTypes.SOCK_ROOMLEAVE):
    case (socketTypes.SOCK_USERLEAVE):
    case (socketTypes.SOCK_JOIN): {
      return state.set(action.message.user.username, Immutable.fromJS(action.message.user));
    }
    case (socketTypes.SOCK_MESSAGE): {
      return state.set(action.message.user.username, Immutable.fromJS(action.message.user));
    }
    case (socketTypes.SOCK_MEMBERS): {
      let nextState = state;
      for (const user of action.message.users) {
        nextState = nextState.set(user.username, Immutable.fromJS(user));
      }
      return nextState;
    }
    case (socketTypes.SOCK_UPDATE_USER): {
      return state.set(action.message.user.username, Immutable.fromJS(action.message.user));
    }
    case (socketTypes.SOCK_REFRESH): {
      return stateFromJS(action.state.users);
    }
    default:
      return state;
  }
}
