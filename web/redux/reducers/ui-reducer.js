import Immutable from 'immutable';
import * as menuTypes from '../constants/menu-constants'
import * as socketTypes from '../constants/socket-constants'
import * as chatTypes from '../constants/chat-constants'


export default function ui(state=Immutable.Map({connected: false, sidebar_open: false, settings_open: false}), action) {
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
    case(chatTypes.CHAT_LINK_COLLAPSED): {
      return state.set('collapsedLinks', state.get('collapsedLinks', Immutable.Set()).add(action.messageId));
    }
    case(chatTypes.CHAT_LINK_EXPANDED): {
      return state.set('collapsedLinks', state.get('collapsedLinks', Immutable.Set()).delete(action.messageId));
    }
    case(chatTypes.CHAT_BLURRED): {
      return state.set('__HAS_FOCUS__', false);
    }
    case(chatTypes.CHAT_FOCUSED): {
      return state.set('__HAS_FOCUS__', true);
    }
    case(chatTypes.CHAT_ROOM_CHANGED): {
      return state.set('__HAS_FOCUS__', true);
    }
    default: {
      return state
    }
  }
}
