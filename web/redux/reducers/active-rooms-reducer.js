import Immutable from 'immutable';

import * as activeRoomsTypes from '../constants/active-rooms-contants';

export default function activeRooms(state = Immutable.Map(), action) {
  switch (action.type) {
    case(activeRoomsTypes.ACTIVE_ROOMS_LOADED_MORE): {
      console.log("REDUCER action: ", action);
      return state.merge(Immutable.fromJS(action.activeRooms.reduce((obj, room) => {
        const newObj = obj;
        newObj[room.rank] = room;
        return newObj;
      }, {})));
    }
    default:
      return state;
  }
}