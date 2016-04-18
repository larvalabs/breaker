import configureStore from './configureStore';
import Immutable from 'immutable'

export default configureStore(Immutable.fromJS(window.__INITIAL_STATE__));
