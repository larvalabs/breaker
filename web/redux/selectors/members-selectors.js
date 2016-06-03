import { createSelector } from 'reselect';
import Immutable from 'immutable';


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
      return membersForCurrentRoom.get('mods', Immutable.List());
    }
);

export const getOnlineForCurrentRoom = createSelector(
    [getMembersForCurrentRoom],
    (membersForCurrentRoom) => {
      return membersForCurrentRoom.get('online', Immutable.List());
    }
);

export const getOfflineForCurrentRoom = createSelector(
    [getMembersForCurrentRoom],
    (membersForCurrentRoom) => {
      return membersForCurrentRoom.get('offline', Immutable.List());
    }
);
export const getAllMembersForCurrentRoom = createSelector(
    [getModsForCurrentRoom, getOnlineForCurrentRoom, getOfflineForCurrentRoom],
    (modUserNames, onlineUserNames, offlineUserNames) => {
      return modUserNames.union(onlineUserNames).union(offlineUserNames);
    }
);
