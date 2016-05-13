import * as socketTypes from '../constants/socket-constants'
import Immutable from 'immutable'
import stateFromJS from '../../util/stateFromJS'


export default function messageEntities(state=Immutable.Set(), action) {
  switch (action.type) {
    case (socketTypes.SOCK_SERVER):
    case (socketTypes.SOCK_UPDATE_MESSAGE):
    case (socketTypes.SOCK_MESSAGE): {
      return state.set(action.message.message.uuid, Immutable.fromJS(action.message.message));
    }
    case(socketTypes.SOCK_REFRESH): {
      return stateFromJS(action.state.messages)
    }
    default:
      return state
  }
}
