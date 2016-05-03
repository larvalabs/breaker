import configureStore from './configureStore';
import Immutable from 'immutable'

export default configureStore(Immutable.fromJS(window.__INITIAL_STATE__, (key, value) => {
  if(key === "rooms"){
    return value.toOrderedMap();
  }
  if(key === "online" || key === "offline" || key === "mods"){
    return value.toOrderedSet();
  }

  var isIndexed = Immutable.Iterable.isIndexed(value);
  return isIndexed ? value.toList() : value.toMap();
}));
