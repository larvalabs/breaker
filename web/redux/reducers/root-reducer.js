import ui from './ui-reducer';
import bannerMessage from './banner-message-reducer';
import currentRoom from './current-room-reducer';
import unreadCounts from './unread-reducer';
import members from './members-reducer';
import roomMessages from './room-messages-reducer';
import messages from './message-entites-reducer';
import rooms from './rooms-reducer';
import users from './users-reducer';

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
  unreadCounts,
  bannerMessage,
  ui
});

export default App;
