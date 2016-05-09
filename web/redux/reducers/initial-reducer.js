import * as chatTypes from '../constants/chat-constants'
import Immutable from 'immutable'


export default function initial(state=Immutable.Map(), action) {
  switch(action.type){
    case(chatTypes.CHAT_ROOM_CHANGED):
      return state.set('roomName', action.roomName);
    default: {
      return state
    }
  }
}
