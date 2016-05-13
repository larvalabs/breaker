import * as socketTypes from '../constants/socket-constants'
import Immutable from 'immutable'
import stateFromJS from '../../util/stateFromJS'


function moveMemberOfflineState(state, action){
  return moveMemberStates(state, action, 'online', 'offline');
}

function moveMemberOnlineState(state, action){
  return moveMemberStates(state, action, 'offline', 'online');
}

function moveMemberStates(state, action, remove, add){
  // Remove previous state
  let newState = state.updateIn([action.message.room.name, remove], o => {
    if(!o) {
      return Immutable.OrderedSet([]);
    }
    return o.delete(action.message.user.username)
  });

  // Add current state
  return newState.updateIn([action.message.room.name, add], o => {
    if(!o){
      return Immutable.OrderedSet([action.message.user.username]);
    }
    return o.add(action.message.user.username).sort((a,b) => b.toLowerCase() - a.toLowerCase());
  });
}

export default function members(state=Immutable.Map(), action) {
  switch(action.type){
    case (socketTypes.SOCK_MEMBERS): {
      return state
    }
    case (socketTypes.SOCK_JOIN): {
      return moveMemberOnlineState(state, action);
    }
    case (socketTypes.SOCK_LEAVE): {
      return moveMemberOfflineState(state, action);
    }
    case(socketTypes.SOCK_REFRESH): {
      return stateFromJS(action.state.members)
    }
    default:
      return state;
  }
}
