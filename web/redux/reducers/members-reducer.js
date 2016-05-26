import Immutable from 'immutable';

import * as socketTypes from '../constants/socket-constants';

import stateFromJS from '../../util/stateFromJS';


function moveMemberStates(state, action, remove, add) {
  // Remove previous state
  const newState = state.updateIn([action.message.room.name, remove], o => {
    if (!o) {
      return Immutable.OrderedSet([]);
    }

    return o.delete(action.message.user.username);
  });

  // Add current state
  return newState.updateIn([action.message.room.name, add], o => {
    if (!o) {
      return Immutable.OrderedSet([action.message.user.username]);
    }
    return o.add(action.message.user.username).sort((a, b) => b.toLowerCase() - a.toLowerCase());
  });
}

function moveMemberOfflineState(state, action) {
  return moveMemberStates(state, action, 'online', 'offline');
}

function moveMemberOnlineState(state, action) {
  return moveMemberStates(state, action, 'offline', 'online');
}

function deleteFrom(state, roomName, setName, username) {
  return state.updateIn([roomName, setName], o => {
    if (!o) {
      return Immutable.OrderedSet([]);
    }
    return o.delete(username);
  });
}

function removeMemberAllStates(state, action) {
  let newState = deleteFrom(state, state, action.message.room.name, 'offline', action.message.user.username);
  newState = deleteFrom(newState, action.message.room.name, 'online', action.message.user.username);
  return deleteFrom(newState, action.message.room.name, 'mods', action.message.user.username);
}

export default function members(state = Immutable.Map(), action) {
  switch (action.type) {
    case (socketTypes.SOCK_MEMBERS): {
      return state;
    }
    case (socketTypes.SOCK_JOIN): {
      return moveMemberOnlineState(state, action);
    }
    case (socketTypes.SOCK_LEAVE): {
      return moveMemberOfflineState(state, action);
    }
    case (socketTypes.SOCK_USERLEAVE): {
      return removeMemberAllStates(state, action);
    }
    case (socketTypes.SOCK_REFRESH): {
      return stateFromJS(action.state.members);
    }
    default:
      return state;
  }
}
