import { createStore, applyMiddleware, compose } from 'redux';
import thunk from 'redux-thunk';
import rootReducer from '../reducers/reducer';
import DevTools from '../../app/DevTools.js'
import Immutable from 'immutable'

// TODO: hide devtools in prod
export default function configureStore(initialState = Immutable.Map()) {
  let finalCreateStore = compose(
        applyMiddleware(thunk),
        DevTools.instrument()
    )(createStore);

  return finalCreateStore(rootReducer, initialState);
}
