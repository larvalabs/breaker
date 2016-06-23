import { createSelector } from 'reselect';

const getActiveRooms = (state) => state.get('activeRooms');

export const getAllActiveRooms = createSelector(
  [getActiveRooms],
  (activeRooms) => {
    return activeRooms;
  }
);

export const findLastRank = createSelector(
  [getActiveRooms],
  (activeRooms) => {
    let rank = 0;
    activeRooms.toArray().forEach(room => {
      if (rank < room.get('rank')) {
        rank = room.get('rank');
      }
    });
    return rank;
  }
);
