import * as socketTypes from '../constants/socket-constants'
import * as chatTypes from '../constants/chat-constants'
import Immutable from 'immutable'
import { combineReducers } from 'redux-immutable';

function users(state=Immutable.Map(), action) {
  switch (action.type) {
    case (socketTypes.SOCK_LEAVE):
    case (socketTypes.SOCK_JOIN): {
      return state.set(action.message.user.username, Immutable.Map(action.message.user))
    }
    case (socketTypes.SOCK_MESSAGE): {
      return state.set(action.message.user.username, Immutable.fromJS(action.message.user));
    }
    case (socketTypes.SOCK_MEMBERS): {
      var nextState = state;
      for (var user of action.message.users) {
        nextState = nextState.set(user.username, Immutable.fromJS(user));
      }
      return nextState;
    }
    default:
      return state
  }
}

function rooms(state=Immutable.Map(), action) {
  switch (action.type) {
    case (socketTypes.SOCK_MESSAGE):
    case (socketTypes.SOCK_MEMBERS): {
      return state.set(action.message.room.name, Immutable.fromJS(action.message.room));
    }
    case (socketTypes.SOCK_ROOM_LIST): {
      var nextState = state;
      for (var room of action.message.rooms) {
        nextState = nextState.set(room.name, Immutable.fromJS(room));
      }
      return nextState;
    }
    default:
      return state
  }
}

function messages(state=Immutable.Map(), action) {
  switch (action.type) {
    case (socketTypes.SOCK_MESSAGE): {
      var message = Immutable.fromJS(action.message.message)
          .set('username', action.message.user.username)
          .set('room', action.message.room.name)
          .delete('user')
          .delete('room');
      
      return state.set(action.message.room.name, state.get(action.message.room.name, Immutable.List()).push(message));
    }
    default:
      return state
  }
}

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
      return Immutable.Set([]);
    }
    return o.remove(action.message.user.username)
  });

  // Add current state
  return newState.updateIn([action.message.room.name, add], o => {
    if(!o){
      return Immutable.Set([action.message.user.username]);
    }
    return o.add(action.message.user.username);
  });
}

function members(state=Immutable.Map(), action) {
  switch(action.type){
    case (socketTypes.SOCK_MEMBERS): {
      let listOfModMembers = action.message.users.filter(user => user.modForRoom).map(user => user.username);
      let listOfOnlineMembers = action.message.users.filter(user => !user.modForRoom && user.online).map(user => user.username);
      let listOfOffline = action.message.users.filter(user => !user.modForRoom && !user.online).map(user => user.username);
      let newMemberList = Immutable.Map({
        online: Immutable.Set(listOfOnlineMembers),
        offline: Immutable.Set(listOfOffline),
        mods: Immutable.Set(listOfModMembers)
      });

      return state.set(action.message.room.name, newMemberList);
    }
    case (socketTypes.SOCK_JOIN): {
      if(state.getIn([action.message.room.name, 'mods', action.message.user.username])){
        return state
      }

      return moveMemberOnlineState(state, action);
    }
    case (socketTypes.SOCK_LEAVE): {
      if(state.getIn([action.message.room.name, 'mods', action.message.user.username])){
        return state
      }

      return moveMemberOfflineState(state, action);
    }
    default:
      return state;
  }
}

function unreadCounts(state=Immutable.Map(), action) {
  switch(action.type){
    case(socketTypes.SOCK_MESSAGE): {
      if(!state.get('__HAS_FOCUS__')){
        return state.update(action.message.room.name, 0, c => c + 1)
      }

      return state
    }
    case(chatTypes.CHAT_BLURRED): {
      return state.set('__HAS_FOCUS__', false);
    }
    case(chatTypes.CHAT_FOCUSED): {
      return state.set(action.roomName, 0).set('__HAS_FOCUS__', true);
    }default: {
      return state
    }
  }
}

function initial(state=Immutable.Map(), action) {
  return state;
}

const App = combineReducers({
  initial,
  members,
  users,
  rooms,
  messages,
  unreadCounts
});

export default App;
