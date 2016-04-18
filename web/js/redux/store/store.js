import configureStore from './configureStore.js';
import Immutable from 'immutable'

export default configureStore(Immutable.fromJS(window.__INITIAL_STATE__));
