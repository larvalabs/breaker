import store from './redux/store/store'
import * as socketActions from './redux/actions/socket-actions'
import Config from './config'

var socket = null;

var makeMessage = function (roomName, message) {
  var msg = {};
  msg.roomName = roomName;
  msg.message = message;
  return msg;
};

function init(websocketUrl) {
  websocketUrl = websocketUrl.replace('ws:', 'wss:');
  socket = new ReconnectingWebSocket(websocketUrl);

  socket.onopen = function (event) {
    store.dispatch(socketActions.onSocketOpen())
  };

  socket.onclose = function (event) {
    store.dispatch(socketActions.onSocketClose())
  };

  socket.onmessage = function (event) {
    const eventData = JSON.parse(event.data);

    // Hack
    if(eventData.type === "roomlist"){
      var messageObj = makeMessage(Config.room_name, '##memberlist##');
      socket.send(JSON.stringify(messageObj));
    }

    store.dispatch(socketActions.onSocketMessage(eventData))
  };
}

export default function (url) {
  if (!socket){
    init(url);
  }

  return socket;
}
