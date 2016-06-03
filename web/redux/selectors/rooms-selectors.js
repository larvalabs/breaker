import { createSelector } from 'reselect';
import Immutable from 'immutable';

import Config from '../../config';

const getRooms = (state) => state.get('rooms');
const getCurrentRoomName = (state) => state.get('currentRoom');

export const getAllRooms = createSelector(
  [getRooms, getCurrentRoomName],
  (rooms, currentRoomName) => {
    if (Config.guest) {
      return rooms.filter(room => room.get('name') === currentRoomName);
    }

    return rooms;
  }
);

export const getCurrentRoom = createSelector(
    [getRooms, getCurrentRoomName],
    (rooms, currentRoomName) => {
      return rooms.get(currentRoomName, Immutable.Map());
    }
);

export const getCurrentRoomStyles = createSelector(
    [getCurrentRoom],
    (currentRoom) => {
      return currentRoom.get('styles', Immutable.Map());
    }
);
