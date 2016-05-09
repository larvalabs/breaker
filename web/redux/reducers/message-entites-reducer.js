import * as socketTypes from '../constants/socket-constants'
import Immutable from 'immutable'


export default function messageEntities(state=Immutable.Set(), action) {
  switch (action.type) {
    case (socketTypes.SOCK_SERVER):
    case (socketTypes.SOCK_MESSAGE): {
      return state.set(action.message.message.uuid, Immutable.fromJS(action.message.message));
    }
    default:
      return state
  }
}
