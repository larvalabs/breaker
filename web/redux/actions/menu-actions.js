import * as actions from '../constants/menu-constants';

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
