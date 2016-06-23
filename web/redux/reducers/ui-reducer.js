import Immutable from 'immutable';

import * as menuTypes from '../constants/menu-constants';
import * as socketTypes from '../constants/socket-constants';
import * as chatTypes from '../constants/chat-constants';
import * as scrollTypes from '../constants/scroll-constants';
import * as activeRoomsTypes from '../constants/active-rooms-contants';


const initialState = Immutable.Map({
  connected: false,
  sidebar_open: false,
  settings_open: false
});

export default function ui(state = initialState, action) {
  switch (action.type) {
    case (socketTypes.SOCK_CLOSE): {
      return state.set('connected', false);
    }
    case (socketTypes.SOCK_OPEN): {
      return state.set('connected', true);
    }
    case (menuTypes.UI_SIDEBAR_OPEN): {
      return state.set('sidebar_open', true);
    }
    case (menuTypes.UI_SIDEBAR_CLOSE): {
      return state.set('sidebar_open', false);
    }
    case (menuTypes.UI_SETTINGS_OPEN): {
      return state.set('settings_open', true);
    }
    case (menuTypes.UI_SETTINGS_CLOSE): {
      return state.set('settings_open', false);
    }
    case (chatTypes.CHAT_LINK_COLLAPSED): {
      return state.set(
          'collapsedLinks',
          state.get('collapsedLinks', Immutable.Set()).add(action.messageId)
      );
    }
    case (chatTypes.CHAT_LINK_EXPANDED): {
      return state.set(
          'collapsedLinks',
          state.get('collapsedLinks', Immutable.Set()).delete(action.messageId)
      );
    }
    case (chatTypes.CHAT_SET_INPUT_FOCUS): {
      return state.set('setInputFocus', true);
    }
    case (chatTypes.CHAT_RESET_INPUT_FOCUS): {
      return state.set('setInputFocus', false);
    }
    case (scrollTypes.SCROLL_TO_MESSAGE_ID): {
      return state.set('scrollToMessageId', action.messageId);
    }
    case (scrollTypes.SCROLL_TO_MESSAGE_ID_RESET): {
      return state.set('scrollToMessageId', null);
    }
    case (scrollTypes.SCROLL_TO_ROOM_NAME): {
      return state.set('scrollToRoomName', action.roomName);
    }
    case (scrollTypes.SCROLL_TO_ROOM_NAME_RESET): {
      return state.set('scrollToRoomName', null);
    }
    case (chatTypes.CHAT_LOADING_MESSAGES): {
      return state.set('moreMessagesLoading', true);
    }
    case (chatTypes.CHAT_LOADED_MESSAGES): {
      return state.set('moreMessagesLoading', false);
    }
    case (chatTypes.CHAT_FAILED_LOADING_MESSAGES): {
      return state.set('moreMessagesLoading', false);
    }
    case (chatTypes.CHAT_BLURRED): {
      return state.set('__HAS_FOCUS__', false);
    }
    case (chatTypes.CHAT_FOCUSED): {
      return state.set('__HAS_FOCUS__', true);
    }
    case (chatTypes.CHAT_ROOM_CHANGED): {
      return state.set('__HAS_FOCUS__', true);
    }
    case (activeRoomsTypes.ACTIVE_ROOMS_LOADING_MORE): {
      return state.set('moreActiveRoomsLoading', true);
    }
    case (activeRoomsTypes.ACTIVE_ROOMS_LOADED_MORE): {
      return state.set('moreActiveRoomsLoading', false);
    }
    case (activeRoomsTypes.ACTIVE_ROOMS_FAILED_LOADING_MORE): {
      return state.set('moreActiveRoomsLoading', false);
    }
    case (activeRoomsTypes.ACTIVE_ROOMS_COMPLETE): {
      return state.set('activeRoomsComplete', true);
    }
    case (activeRoomsTypes.ACTIVE_ROOMS_RESET): {
      return state.set('activeRoomsComplete', false);
    }
    default: {
      return state;
    }
  }
}
