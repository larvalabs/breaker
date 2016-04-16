import * as socketTypes from '../constants/socket-constants.js'

function users(state={}, action) {
  switch (action.type) {
    case (socketTypes.SOCK_MESSAGE): {
      return Object.assign({}, state, {
        [action.message.user.username]: action.message.user
      });
    }
    case (socketTypes.SOCK_MEMBERS): {
      var nextState = Object.assign({}, state);
      for (var user of action.message.users) {
        nextState[user.username] = user;
      }
      return nextState;
    }
    default:
      return state
  }
}

function rooms(state={}, action) {
  switch (action.type) {
    case (socketTypes.SOCK_MESSAGE):
    case (socketTypes.SOCK_MEMBERS): {
      return Object.assign({}, state, {
        [action.message.room.name]: action.message.room
      });
    }
    case (socketTypes.SOCK_ROOM_LIST): {
      var nextState = Object.assign({}, state);
      for (var room of action.message.rooms) {
        nextState[room.name] = room;
      }
      return nextState;
    }
    default:
      return state
  }
}

function messages(state={}, action) {
  switch (action.type) {
    case (socketTypes.SOCK_MESSAGE): {
      var message = Object.assign({}, action.message.message);
      message.username = message.user.username;
      message.room = message.room.name;
      delete message.user;
      delete message.room;
      return Object.assign({}, state, {
        [action.message.room.name]: [].concat(state[action.message.room.name]).concat(message)
      });
    }
    default:
      return state
  }
}

function members(state={}, action) {
  switch(action.type){
    case (socketTypes.SOCK_MEMBERS): {
      return Object.assign({}, state, {
        [action.message.room.name]: action.message.users.map(user => user.username)
      });
    }
    default:
      return state;
  }
}

function initial(state={}, action) {
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
