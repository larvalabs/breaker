import { createSelector } from 'reselect';

import { getModsForCurrentRoom } from './members-selectors';


const getAuthUser = (state) => state.get('authUser');

export const getAuthUserIsMod = createSelector(
    [getAuthUser, getModsForCurrentRoom],
    (authUser, modUserNames) => {
      return modUserNames.contains(authUser.get('username'));
    }
);
