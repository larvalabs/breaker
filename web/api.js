import request from 'axios'

export var API = {
  requestInitialState(){
    return request.get("/application/initialState")
  }
};
