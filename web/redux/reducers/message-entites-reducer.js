import Immutable from 'immutable';

import stateFromJS from '../../util/stateFromJS';

import * as socketTypes from '../constants/socket-constants';
import * as chatTypes from '../constants/chat-constants';


export default function messageEntities(state = Immutable.Set(), action) {
  switch (action.type) {
    case (socketTypes.SOCK_SERVER):
    case (socketTypes.SOCK_UPDATE_MESSAGE):
    case (socketTypes.SOCK_MESSAGE): {
      return state.set(action.message.message.uuid, Immutable.fromJS(action.message.message));
    }
    case (chatTypes.CHAT_LOADED_MESSAGES): {
      return state.merge(Immutable.fromJS(action.messages.reduce((obj, message) => {
        const newObj = obj;
        newObj[message.uuid] = message;
        return newObj;
      }, {})));
    }
    case (socketTypes.SOCK_REFRESH): {
      return stateFromJS(action.state.messages);
    }
    default:
      return state;
  }
}
