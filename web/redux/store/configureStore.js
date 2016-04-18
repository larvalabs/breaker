import { createStore, applyMiddleware, compose } from 'redux';
import thunk from 'redux-thunk';
import rootReducer from '../reducers/reducer';
import DevTools from '../../app/DevTools'
import Immutable from 'immutable'
import Config from '../../config'

export default function configureStore(initialState = Immutable.Map()) {
  let finalCreateStore;

  if(Config.environment.prod){
    finalCreateStore = applyMiddleware(thunk)(createStore);
  } else {
    finalCreateStore = compose(
        applyMiddleware(thunk),
        DevTools.instrument()
    )(createStore);
  }


  return finalCreateStore(rootReducer, initialState);
}
