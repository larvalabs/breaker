import * as actions from '../constants/menu-constants';
import { API } from '../../api'

function sidebarOpen() {
  return { type: actions.UI_SIDEBAR_OPEN };
}

function sidebarClose() {
  return { type: actions.UI_SIDEBAR_CLOSE };
}

function settingsOpen() {
  return { type: actions.UI_SETTINGS_OPEN };
}

function settingsClose() {
  return { type: actions.UI_SETTINGS_CLOSE };
}

function leaveRoom(newActiveRoom){
  return { type: actions.ROOM_LEAVE, newActiveRoom };
}

export function handleLeaveRoom(roomName) {
  return (dispatch, getState) => {
    API.leaveRoom(roomName).then(() => {
      dispatch(leaveRoom(roomName));
    }).catch((e) => {
      console.log("Failed to leave room", e)
    })
  }
}

export function toggleSidebar() {
  return (dispatch, getState) => {
    
    if(getState().getIn(['ui', 'sidebar_open'], false)){
      return dispatch(sidebarClose())
    }
    return dispatch(sidebarOpen())
  }
}

export function toggleSettings() {
  return (dispatch, getState) => {

    if(getState().getIn(['ui', 'settings_open'], false)){
      return dispatch(settingsClose())
    }
    return dispatch(settingsOpen())
  }
}
