import * as actions from '../constants/notification-constants';
import * as chatActions from '../actions/chat-actions';


export function handleNotSupported() {
  return { type: actions.NOTIFY_NOT_SUPPORTED };
}

export function handlePermissionGranted() {
  return { type: actions.NOTIFY_PERMISSION_GREANTED };
}

export function handlePermissionDenied() {
  return { type: actions.NOTIFY_PERMISSION_DENIED };
}

export function handleNotificationOnShow() {
  return { type: actions.NOTIFY_ON_SHOW };
}

export function handleNotificationOnClick(event) {
  return (dispatch) => {
    window.focus();
    dispatch(chatActions.handleChangeRoom(event.target.data.roomName));
    dispatch({ type: actions.NOTIFY_ON_CLICK });
  };
}

export function handleNotificationOnClose() {
  return { type: actions.NOTIFY_ON_CLOSE };
}

export function handleNotificationOnError() {
  return { type: actions.NOTIFY_ON_ERROR };
}

export function handleSendNotification(title, body, roomName) {
  return { type: actions.NOTIFY_SEND, title, body, roomName };
}

export function handleMessageUserMentionNotification(message) {
  return (dispatch, getStore) => {
    // Prop validation
    if (!message.message.mentionedUsernames || !message.message.mentionedUsernames.length) {
      return null;
    }

    if (message.message.mentionedUsernames.length < 1) {
      return null;
    }

    const authUsername = getStore().getIn(['authUser', 'username'], '').toLowerCase();
    if (message.user.username.toLowerCase() === authUsername) {
      return null;
    }

    if (message.message.mentionedUsernames.indexOf(authUsername) > -1) {
      dispatch(handleSendNotification(message.user.username, message.message.message, message.room.name));
    }
  };
}

export function handleNewMessageNotification(message) {
  return (dispatch, getStore) => {
    // Prop validation
    if (!message || !message.message || !message.user || !message.room ||
        !message.user.username || !message.message.message) {
      return null;
    }

    const store = getStore();
    if (store.get('currentRoom') === message.room.name && store.getIn(['ui', '__HAS_FOCUS__'])) {
      return null;
    }

    dispatch(handleMessageUserMentionNotification(message));
  };
}
