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
