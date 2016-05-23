import ui from './ui-reducer';
import bannerMessage from './banner-message-reducer';
import currentRoom from './current-room-reducer';
import members from './members-reducer';
import roomMessages from './room-messages-reducer';
import messages from './message-entites-reducer';
import rooms from './rooms-reducer';
import users from './users-reducer';
import lastSeenTimes from './last-seen-reducer';
import notification from './notification-reducer';

import { combineReducers } from 'redux-immutable';

function authUser(state=Immutable.Map(), action) {
  return state
}

const App = combineReducers({
  currentRoom,
  authUser,
  members,
  users,
  rooms,
  roomMessages,
  messages,
  bannerMessage,
  ui,
  lastSeenTimes,
  notification
});

export default App;
