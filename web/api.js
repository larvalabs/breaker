import request from 'axios'

export var API = {
  requestInitialState(){
    return request.get("/application/initialState")
  },

  leaveRoom(roomName){
    return request.get("/application/leaveRoom?roomName=" + roomName)
  },

  fetchMoreMessages(roomName, fromMessageId){
    let count = 10;
    return request.get(`/application/getmessages?roomName=${roomName}&id=${fromMessageId}&limit=${count}&before=true`)
  }
};
