import request from 'axios'

export var API = {
  requestInitialState(){
    return request.get("/application/initialState")
  },

  leaveRoom(roomName){
    return request.get("/application/leaveRoom?roomName=" + roomName)
  }
};
