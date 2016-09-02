import request from 'axios';


const API = {
  requestInitialState() {
    return request.get('/application/initialState');
  },

  leaveRoom(roomName) {
    return request.get(`/application/leaveRoom?roomName=${roomName}`);
  },

  fetchMoreMessages(roomName, fromMessageId, count) {
    return request.get(`/application/getmessages?roomName=${roomName}&id=${fromMessageId}&limit=${count}&before=true`);
  },

  fetchMoreActiveRooms(limit, offset) {
    return request.get(`/application/getactiverooms?limit=${limit}&offset=${offset}`);
  },

  deleteMessage(messageUuid) {
    return request.get(`/application/deletemessage?uuid=${messageUuid}`);
  }

};

export default API;
