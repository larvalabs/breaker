import ui from './ui-reducer';
import message from './message-reducer';
import initial from './initial-reducer';
import unreadCounts from './unread-reducer';
import members from './members-reducer';
import messages from './messages-reducer';
import rooms from './rooms-reducer';
import users from './users-reducer';

import { combineReducers } from 'redux-immutable';

const App = combineReducers({
  initial,
  members,
  users,
  rooms,
  messages,
  unreadCounts,
  message,
  ui
});

export default App;
