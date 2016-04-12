import io from 'socket.io';

var socket = null;

function init(websocketUrl) {
  // See https://github.com/joewalnes/reconnecting-websocket for options:;
  websocketUrl = websocketUrl.replace('ws:', 'wss:');
  socket = new ReconnectingWebSocket(websocketUrl);
  // socket.debug = true;

  socket.onopen = function (event) {
//        console.log("Socket state: "+socket.readyState);
    if (!firstConnect) {
      Messenger().post({
        message: 'Connected!',
        type: 'success'
      });
    }
    firstConnect = false;

    $('.input-message').prop("disabled", false);
  };

  socket.onclose = function (event) {
    Messenger().post({
      message: 'Disconnected from server, will retry...',
      type: 'error'
    });
    $('.input-message').prop("disabled", true);
  };

  // Message received on the socket
  socket.onmessage = function (event) {
//            console.log(event.data);
    var eventObj = JSON.parse(event.data);

    display(eventObj);
  };
}

export default function (url) {
  if (!socket){
    init(url);
  }

  return socket;
}
