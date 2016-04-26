import configureStore from './configureStore';
import Immutable from 'immutable'

let initialState = Immutable.Map({
  message: Immutable.fromJS(window.__INITIAL_STATE__.message),
  initial: Immutable.fromJS(window.__INITIAL_STATE__.initial),
  members: Immutable.Map({
    mods: Immutable.Set(window.__INITIAL_STATE__.members.mods),
    online: Immutable.Set(window.__INITIAL_STATE__.members.online),
    offline: Immutable.Set(window.__INITIAL_STATE__.members.offline)
  }),
  rooms: Immutable.fromJS(window.__INITIAL_STATE__.rooms),
  users: Immutable.fromJS(window.__INITIAL_STATE__.users),
  messages: Immutable.fromJS(window.__INITIAL_STATE__.messages),
  unreadCounts: Immutable.fromJS(window.__INITIAL_STATE__.unreadCounts)
});

export default configureStore(initialState);
