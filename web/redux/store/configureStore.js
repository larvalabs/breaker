import { createStore, applyMiddleware, compose } from 'redux';
import thunk from 'redux-thunk';
import Immutable from 'immutable';

import Config from '../../config';
import rootReducer from '../reducers/root-reducer';

import DevTools from '../../dev/DevTools';


export default function configureStore(initialState = Immutable.Map()) {
  let finalCreateStore;

  if (!Config.settings.dev_tools) {
    finalCreateStore = applyMiddleware(thunk)(createStore);
  } else {
    finalCreateStore = compose(
        applyMiddleware(thunk),
        DevTools.instrument()
    )(createStore);
  }

  return finalCreateStore(rootReducer, initialState);
}
