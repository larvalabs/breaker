import Immutable from 'immutable';


export default function stateFromJS(state) {
  return Immutable.fromJS(state, (key, value) => {
    if (key === 'rooms') {
      return value.toOrderedMap();
    }

    if (key === 'online' || key === 'offline' || key === 'mods') {
      return value.toOrderedSet();
    }

    const isIndexed = Immutable.Iterable.isIndexed(value);
    return isIndexed ? value.toList() : value.toOrderedMap();
  });
}
