import * as chatTypes from '../constants/chat-constants';


export default function currentRoom(state = null, action) {
  switch (action.type) {
    case (chatTypes.CHAT_ROOM_CHANGED):
      return action.roomName;
    default: {
      return state;
    }
  }
}
