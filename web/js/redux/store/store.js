import configureStore from './configureStore.js';
import Immutable from 'immutable'

window.__INITIAL_STATE__.initial = Immutable.fromJS(window.__INITIAL_STATE__.initial);
window.__INITIAL_STATE__.rooms = Immutable.fromJS(window.__INITIAL_STATE__.rooms);
window.__INITIAL_STATE__.users = Immutable.fromJS(window.__INITIAL_STATE__.users);
window.__INITIAL_STATE__.messages = Immutable.fromJS(window.__INITIAL_STATE__.messages);
window.__INITIAL_STATE__.members = Immutable.fromJS(window.__INITIAL_STATE__.members);

export default configureStore(window.__INITIAL_STATE__);
