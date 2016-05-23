import * as actions from '../constants/notification-constants';


export function handleNotSupported(){
  return {type: actions.NOTIFY_NOT_SUPPORTED}
}

export function handlePermissionGranted(){
  return {type: actions.NOTIFY_PERMISSION_GREANTED}
}

export function handlePermissionDenied(){
  return {type: actions.NOTIFY_PERMISSION_DENIED}
}

export function handleNotificationOnShow(){
  return {type: actions.NOTIFY_ON_SHOW}
}

export function handleNotificationOnClick(){
  return {type: actions.NOTIFY_ON_CLICK}
}

export function handleNotificationOnClose(){
  return {type: actions.NOTIFY_ON_CLOSE}

}

export function handleNotificationOnError(){
  return {type: actions.NOTIFY_ON_ERROR}
}

export function handleSendNotification(title, body) {
  return {type: actions.NOTIFY_SEND, title, body}
}

export function handleMessageUserMentionNotification(message) {
  return (dispatch, getStore) => {
    // Prop validation
    if(!message.message.mentionedUsernames || !message.message.mentionedUsernames.length){
      return null
    }

    if(message.message.mentionedUsernames.length < 1){
      return null
    }

    const authUsername = getStore().getIn(['authUser', 'username'], "").toLowerCase();
    if(message.user.username.toLowerCase() === authUsername){
      return null
    }

    if(message.message.mentionedUsernames.indexOf(authUsername) > -1){
      dispatch(handleSendNotification(message.user.username, message.message.message))
    }
  }
}

export function handleNewMessageNotification(message) {
  return (dispatch) => {
    // Prop validation
    if(!message || !message.message || !message.user || !message.user.username || !message.message.message){
      return null
    }

    dispatch(handleMessageUserMentionNotification(message))
  }
}
