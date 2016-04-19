import * as socketTypes from '../constants/socket-constants'
import Immutable from 'immutable'
import { combineReducers } from 'redux-immutable';

function users(state=Immutable.Map(), action) {
  switch (action.type) {
    case (socketTypes.SOCK_MESSAGE): {

      // Hack since the message user object doesn't include mod information
      if(!state.get(action.message.user.username)) {
        return state.set(action.message.user.username, Immutable.fromJS(action.message.user));
      }

      return state
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

function members(state=Immutable.Map(), action) {
  switch(action.type){
    case (socketTypes.SOCK_MEMBERS): {
      let listOfModMembers = action.message.users.filter(user => user.modForRoom).map(user => user.username);
      let listOfOnlineMembers = action.message.users.filter(user => !user.modForRoom && user.online).map(user => user.username);
      let listOfOffline = action.message.users.filter(user => !user.modForRoom && !user.online).map(user => user.username);
      let newMemberList = Immutable.Map({
        online: Immutable.Set(listOfOnlineMembers).toList(),
        offline: Immutable.Set(listOfOffline).toList(),
        mods: Immutable.Set(listOfModMembers).toList()
      });

      return state.set(action.message.room.name, newMemberList);
    }
    default:
      return state;
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
  messages
});

export default App;
