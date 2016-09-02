import Immutable from 'immutable';

import * as socketTypes from '../constants/socket-constants';
import * as chatTypes from '../constants/chat-constants';

import stateFromJS from '../../util/stateFromJS';


export default function roomMessages(state = Immutable.Map(), action) {
  switch (action.type) {
    case (socketTypes.SOCK_SERVER):
    case (socketTypes.SOCK_MESSAGE): {
      return state.set(
          action.message.room.name,
          state.get(
              action.message.room.name,
              Immutable.List()
          ).push(action.message.message.uuid)
      );
    }
    case (socketTypes.SOCK_UPDATE_MESSAGE): {
      if (action.message.message.deleted) {
        return state.set(
            action.message.room.name,
            state.get(
                action.message.room.name,
                Immutable.List()
            ).filter(function(msgUuid) {
              return msgUuid !== action.message.message.uuid;
            })
        );
      }
      return state;
    }
    case (chatTypes.CHAT_LOADED_MESSAGES): {
      return state.update(action.room, (currentMessageList) => {
        return Immutable.List(
            action.messages.filter((message) => !message.deleted).map((message) => message.uuid)
        ).reverse().concat(currentMessageList);
      });
    }
    case (socketTypes.SOCK_REFRESH): {
      return stateFromJS(action.state.roomMessages);
    }
    default:
      return state;
  }
}
