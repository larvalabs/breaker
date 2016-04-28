import * as socketTypes from '../constants/socket-constants'
import * as chatTypes from '../constants/chat-constants'
import * as menuTypes from '../constants/menu-constants'

import Immutable from 'immutable'
import { combineReducers } from 'redux-immutable';

function users(state=Immutable.Map(), action) {
  switch (action.type) {
    case (socketTypes.SOCK_LEAVE):
    case (socketTypes.SOCK_JOIN): {
      return state.set(action.message.user.username, Immutable.fromJS(action.message.user))
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
    case (socketTypes.SOCK_SERVER): {
      let message = Immutable.fromJS(action.message);

      return state.set(action.message.room.name, state.get(action.message.room.name, Immutable.List()).push(message));
    }
    case (socketTypes.SOCK_MESSAGE): {
      let message = Immutable.fromJS(action.message.message)
          .set('username', action.message.user.username)
          .set('room', action.message.room.name)
          .set('type', "message")
          .delete('user')
          .delete('room');
      
      return state.set(action.message.room.name, state.get(action.message.room.name, Immutable.List()).push(message));
    }
    default:
      return state
  }
}


function ensureTypeIsSet(state, roomName, status){
  if(!Immutable.Set.isSet(state.getIn([roomName, status]))){
    return state.setIn([roomName, status], Immutable.Set(
        state.getIn([roomName, status]))
    );
  }
  return state;
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
        online: Immutable.OrderedSet(listOfOnlineMembers),
        offline: Immutable.OrderedSet(listOfOffline),
        mods: Immutable.OrderedSet(listOfModMembers)
      });

      return state.set(action.message.room.name, newMemberList);
    }
    case (socketTypes.SOCK_JOIN): {
      let newState = ensureTypeIsSet(state, action.message.room.name, "mods");
      newState = ensureTypeIsSet(state, action.message.room.name, "online");

      if(newState.getIn([action.message.room.name, 'mods'], Immutable.Set()).has(action.message.user.username)){
        return newState
      }

      if(state.getIn([action.message.room.name, 'online'], Immutable.Set()).has(action.message.user.username)){
        return newState
      }

      return moveMemberOnlineState(newState, action);
    }
    case (socketTypes.SOCK_LEAVE): {
      let newState = ensureTypeIsSet(state, action.message.room.name, "mods");
      newState = ensureTypeIsSet(state, action.message.room.name, "offline");

      if(newState.getIn([action.message.room.name, 'mods'], Immutable.Set()).has(action.message.user.username)){
        return newState
      }

      if(newState.getIn([action.message.room.name, 'offline'], Immutable.Set()).has(action.message.user.username)){
        return newState
      }

      return moveMemberOfflineState(newState, action);
    }
    default:
      return state;
  }
}

function unreadCounts(state=Immutable.Map(), action) {
  switch(action.type){
    case(socketTypes.SOCK_MESSAGE): {
      if(!state.get('__HAS_FOCUS__') && !action.message.user.bot){
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
  switch(action.type){
    case(chatTypes.CHAT_ROOM_CHANGED):
      return state.set('roomName', action.roomName);
    default: {
      return state
    }
  }
}

function message(state=Immutable.Map(), action) {
  switch(action.type){
    case(socketTypes.SOCK_CLOSE): {
      return Immutable.Map({
        type: "error",
        body: "Disconnected from server, will retry..."
      })
    }
    case(socketTypes.SOCK_OPEN): {
      return Immutable.Map()
    }
    default: {
      return state
    }
  }
}

function ui(state=Immutable.Map({connected: false, sidebar_open: false, settings_open: false}), action) {
  switch(action.type){
    case(socketTypes.SOCK_CLOSE): {
      return state.set('connected', false)
    }
    case(socketTypes.SOCK_OPEN): {
      return state.set('connected', true)
    }
    case(menuTypes.UI_SIDEBAR_OPEN): {
      return state.set('sidebar_open', true)
    }
    case(menuTypes.UI_SIDEBAR_CLOSE): {
      return state.set('sidebar_open', false)
    }
    case(menuTypes.UI_SETTINGS_OPEN): {
      return state.set('settings_open', true)
    }
    case(menuTypes.UI_SETTINGS_CLOSE): {
      return state.set('settings_open', false)
    }
    default: {
      return state
    }
  }
}

const App = combineReducers({
  initial,
  members,
  users,
  rooms,
  messages,
  unreadCounts,
  message,
  ui,
});

export default App;
