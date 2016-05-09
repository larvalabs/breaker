import * as socketTypes from '../constants/socket-constants'
import * as chatTypes from '../constants/chat-constants'
import Immutable from 'immutable'


export default function unreadCounts(state=Immutable.Map(), action) {
  switch(action.type){
    case(socketTypes.SOCK_MESSAGE): {
      if(!state.get('__HAS_FOCUS__') && !action.message.user.bot){
        return state.update(action.message.room.name, 0, c => c + 1)
      }

      return state
    }
    case(chatTypes.CHAT_BLURRED): {
      return state.set('__HAS_FOCUS__', false);
    }
    case(chatTypes.CHAT_FOCUSED): {
      return state.set(action.roomName, 0).set('__HAS_FOCUS__', true);
    }
    case(chatTypes.CHAT_ROOM_CHANGED): {
      return state.set('__HAS_FOCUS__', true).set(action.roomName, 0);
    }
    default: {
      return state
    }
  }
}
