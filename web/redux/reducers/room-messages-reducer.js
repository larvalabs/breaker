import * as socketTypes from '../constants/socket-constants'
import Immutable from 'immutable'


export default function roomMessages(state=Immutable.Map(), action) {
  switch (action.type) {
    case (socketTypes.SOCK_SERVER): {
      // TODO: there's no UUID for these
      return state;
      // return state.set(action.message.room.name, state.get(action.message.room.name, Immutable.List()).push(message.uuid));
    }
    case (socketTypes.SOCK_MESSAGE): {
      return state.set(action.message.room.name, state.get(action.message.room.name, Immutable.List()).push(action.message.message.uuid));
    }
    default:
      return state
  }
}
