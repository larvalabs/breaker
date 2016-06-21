import { createSelector } from 'reselect';

const getUserEntities = (state) => state.get('users');

import { getModsForCurrentRoom, getOnlineForCurrentRoom, getOfflineForCurrentRoom } from './members-selectors';

export const getModUsersForCurrentRoom = createSelector(
    [getModsForCurrentRoom, getUserEntities],
    (mods, userEntities) => {
      return mods.map((username) => userEntities.get(username));
    }
);

export const getOnlineUsersForCurrentRoom = createSelector(
    [getOnlineForCurrentRoom, getUserEntities],
    (mods, userEntities) => {
      return mods.filter((username) => username !== 'guest').map((username) => userEntities.get(username));
    }
);

export const getOfflineUsersForCurrentRoom = createSelector(
    [getOfflineForCurrentRoom, getUserEntities],
    (mods, userEntities) => {
      return mods.filter((username) => username !== 'guest').map((username) => userEntities.get(username));
    }
);
