import ui from './ui-reducer';
import message from './message-reducer';
import initial from './initial-reducer';
import unreadCounts from './unread-reducer';
import members from './members-reducer';
import roomMessages from './room-messages-reducer';
import messages from './message-entites-reducer';
import rooms from './rooms-reducer';
import users from './users-reducer';

import { combineReducers } from 'redux-immutable';

const App = combineReducers({
  initial,
  members,
  users,
  rooms,
  roomMessages,
  messages,
  unreadCounts,
  message,
  ui
});

export default App;
