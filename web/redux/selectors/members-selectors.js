import { createSelector } from 'reselect'
import Immutable from 'immutable';

const getAuthUser = (state) => state.get('members');
const getMembers = (state) => state.get('members');
const getCurrentRoomName = (state) => state.get('currentRoom');

export const getMembersForCurrentRoom = createSelector(
    [getMembers, getCurrentRoomName],
    (members, currentRoomName) => {
      return members.get(currentRoomName, Immutable.Map());
    }
);

export const getModsForCurrentRoom = createSelector(
    [getMembersForCurrentRoom],
    (membersForCurrentRoom) => {
      return membersForCurrentRoom.get('mods', Immutable.List())
    }
);

export const getSidebarOpen = createSelector(
    [getModsForCurrentRoom],
    (modUsernames) => {
      let userIsMod;
      if (members && members.get('breakerapp')) {
        return modUsernames.constains(user.get('username'));
      }
    }
);
