import { createStore, applyMiddleware, compose } from 'redux';
import thunk from 'redux-thunk';
import rootReducer from '../reducers/reducer';
import DevTools from '../../app/DevTools.js'

// TODO: hide devtools in prod
export default function configureStore(initialState = {}) {
  let finalCreateStore = compose(
        applyMiddleware(thunk),
        DevTools.instrument()
    )(createStore);

  return finalCreateStore(rootReducer, initialState);
}
