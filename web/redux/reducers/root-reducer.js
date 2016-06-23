import { combineReducers } from 'redux-immutable';
import Immutable from 'immutable';

import ui from './ui-reducer';
import bannerMessage from './banner-message-reducer';
import currentRoom from './current-room-reducer';
import activeRooms from './active-rooms-reducer';
import members from './members-reducer';
import roomMessages from './room-messages-reducer';
import messages from './message-entites-reducer';
import rooms from './rooms-reducer';
import users from './users-reducer';
import lastSeenTimes from './last-seen-reducer';
import notification from './notification-reducer';


function authUser(state = Immutable.Map()) {
  return state;
}

const App = combineReducers({
  currentRoom,
  authUser,
  members,
  users,
  rooms,
  activeRooms,
  roomMessages,
  messages,
  bannerMessage,
  ui,
  lastSeenTimes,
  notification
});

export default App;
