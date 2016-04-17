import * as socketTypes from '../constants/socket-constants.js'
import Immutable from 'immutable'

function users(state=Immutable.Map(), action) {
  switch (action.type) {
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

function members(state=Immutable.Map(), action) {
  switch(action.type){
    case (socketTypes.SOCK_MEMBERS): {
      let listOfMembers = action.message.users.map(user => user.username);
      return state.set(action.message.room.name, Immutable.fromJS(listOfMembers))
    }
    default:
      return state;
  }
}

function initial(state=Immutable.Map(), action) {
  return state;
}

export default function rootReducer(state={}, action){
  return {
    initial: initial(state.initial, action),
    members: members(state.members, action),
    users: users(state.users, action),
    rooms: rooms(state.rooms, action),
    messages: messages(state.messages, action)
  }
}
