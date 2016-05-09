import store from './redux/store/store'
import * as chatActions from './redux/actions/chat-actions'

function onFocus(){
  store.dispatch(chatActions.chatFocused(store.getState().get('currentRoom')))
}

function onBlur(){
  store.dispatch(chatActions.chatBlurred(store.getState().get('currentRoom')))
}

export default function register(){
  if (/*@cc_on!@*/false) { // check for Internet Explorer
    document.onfocusin = onFocus;
    document.onfocusout = onBlur;
  } else {
    window.onfocus = onFocus;
    window.onblur = onBlur;
  }
}
