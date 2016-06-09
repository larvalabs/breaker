import { createSelector } from 'reselect';

const getActiveRooms = (state) => state.get('activeRooms');

export const getAllActiveRooms = createSelector(
    [getActiveRooms],
    (activeRooms) => {
        console.log("ACTIVE CHANNELS IN SELECTOR: ", activeRooms);
        return activeRooms;
    }
);