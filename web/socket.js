import store from './redux/store/store';
import * as socketActions from './redux/actions/socket-actions';
import Config from './config';


let socket = null;
let lastPingTime = null;
const ReconnectingWebSocket = window.ReconnectingWebSocket;

function init() {
  const websocketUrl = Config.websocket_url.replace('ws:', 'wss:');
  socket = new ReconnectingWebSocket(websocketUrl);
  let pingTimeout = null;

  socket.onopen = () => {
    store.dispatch(socketActions.handleSocketOpen(store, socket));
  };

  socket.onclose = () => {
    store.dispatch(socketActions.handleSocketClose(store, socket));
  };

  socket.onmessage = (event) => {
    const eventData = JSON.parse(event.data);
    store.dispatch(socketActions.onSocketMessage(eventData));
  };

  socket.startRoomPing = (room) => {
    if (pingTimeout) {
      window.clearInterval(pingTimeout);
    }

    if (!lastPingTime) {
      lastPingTime = Date.now();
    }

    pingTimeout = window.setInterval(() => {
      if (socket.readyState !== 1) {
        console.log("Can't ping, connection not open.");  // eslint-disable-line
        return;
      }

      if (Date.now() - lastPingTime > Config.settings.max_stale_state_millis) {
        store.dispatch(socketActions.handleStateRefresh());
      }

      lastPingTime = Date.now();
      socket.sendPing(room);
    }, Config.settings.ping_timeout);
  };

  socket.stopRoomPing = () => {
    if (pingTimeout) {
      window.clearInterval(pingTimeout);
    }
  };

  socket.sendPing = (room) => {
    socket.send(JSON.stringify({
      message: '##ping##',
      roomName: room
    }));
  };

  socket.sendMemberList = (room) => {
    socket.send(JSON.stringify({
      message: '##memberlist##',
      roomName: room
    }));
  };

  socket.sendRoomMessagesSeen = (room) => {
    socket.send(JSON.stringify({
      message: '##markmessagesread##',
      roomName: room
    }));
  };
}

export default function () {
  if (!socket) {
    init();
  }

  return socket;
}
