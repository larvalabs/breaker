import configureStore from './configureStore';
import stateFromJS from '../../util/stateFromJS'

export default configureStore(stateFromJS(window.__INITIAL_STATE__));
