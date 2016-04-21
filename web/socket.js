import store from './redux/store/store'
import * as socketActions from './redux/actions/socket-actions'
import Config from './config'

var socket = null;

function init() {
  let websocketUrl = Config.websocket_url.replace('ws:', 'wss:');
  socket = new ReconnectingWebSocket(websocketUrl);
  let pingTimeouts = [];

  socket.onopen = function (event) {
    store.dispatch(socketActions.handleSocketOpen(store, socket))
  };

  socket.onclose = function (event) {
    store.dispatch(socketActions.handleSocketClose(store, socket))
  };

  socket.onmessage = function (event) {
    const eventData = JSON.parse(event.data);
    if(eventData.type === "roomlist"){
      for(let room of eventData.rooms){
        socket.sendMemeberList(room.name);
      }
    } 
    store.dispatch(socketActions.onSocketMessage(eventData))
  };
  
  socket.startRoomPing = function (room) {
    pingTimeouts[room] = window.setInterval(function () {
      if (socket.readyState !== 1) {
        console.log("Can't ping, connection not open.");
        return;
      }
      
      socket.sendPing(room);
        
    }, Config.settings.ping_timeout);
  };
  
  socket.stopRoomPing = function (room) {
    if(pingTimeouts[room]){
      window.clearInterval(pingTimeouts[room]);
    }
  };
  
  socket.sendPing = function(room){
    socket.send(JSON.stringify({
      message: "##ping##",
      roomName: room
    }));
  };
  
  socket.sendMemeberList = function (room) {
    socket.send(JSON.stringify({
      message: "##memberlist##",
      roomName: room
    }));
  };
}

export default function () {
  if (!socket){
    init();
  }

  return socket;
}
