import * as socketTypes from '../constants/socket-constants'
import * as chatTypes from '../constants/chat-constants'
import Immutable from 'immutable'
import stateFromJS from '../../util/stateFromJS'


export default function messageEntities(state=Immutable.Set(), action) {
  switch (action.type) {
    case (socketTypes.SOCK_SERVER):
    case (socketTypes.SOCK_UPDATE_MESSAGE):
    case (socketTypes.SOCK_MESSAGE): {
      return state.set(action.message.message.uuid, Immutable.fromJS(action.message.message));
    }
    case (chatTypes.CHAT_LOADED_MESSAGES): {
      return state.merge(Immutable.fromJS(action.messages.reduce((obj, message) => {
        obj[message.uuid] = message;
        return obj
      }, {})));
    }
    case(socketTypes.SOCK_REFRESH): {
      return stateFromJS(action.state.messages)
    }
    default:
      return state
  }
}
