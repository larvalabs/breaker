import Immutable from 'immutable';

import * as activeRoomsTypes from '../constants/active-rooms-contants';

export default function activeRooms(state = Immutable.Map(), action) {
  switch (action.type) {
    case (activeRoomsTypes.ACTIVE_ROOMS_LOADED_MORE): {
      return state.merge(Immutable.fromJS(action.activeRooms.reduce((obj, room) => {
        const newObj = obj;
        newObj[room.name] = room;
        return newObj;
      }, {})));
    }
    default:
      return state;
  }
}
