import store from './redux/store/store.js'
import * as socketActions from './redux/actions/socket-actions.js'

var socket = null;

function init(websocketUrl) {
  websocketUrl = websocketUrl.replace('ws:', 'wss:');
  socket = new ReconnectingWebSocket(websocketUrl);

  socket.onopen = function (event) {
    store.dispatch(socketActions.onSocketOpen(firstConnect))
  };

  socket.onclose = function (event) {
    store.dispatch(socketActions.onSocketClose())
  };

  socket.onmessage = function (event) {
    store.dispatch(socketActions.onSocketMessage(JSON.parse(event.data)))
  };
}

export default function (url) {
  if (!socket){
    init(url);
  }

  return socket;
}
