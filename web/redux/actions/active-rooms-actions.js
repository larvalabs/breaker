import API from '../../api';

import * as actions from '../constants/active-rooms-contants';
import { findLastRank } from '../selectors/active-rooms-selector';


export function loadingMoreActiveRooms() {
  return { type: actions.ACTIVE_ROOMS_LOADING_MORE };
}

export function loadedMoreActiveRooms(activeRooms) {
  return { type: actions.ACTIVE_ROOMS_LOADED_MORE, activeRooms };
}

export function failedLoadingMoreActiveRooms(error) {
  return { type: actions.ACTIVE_ROOMS_FAILED_LOADING_MORE, error };
}

export function setActiveRoomsComplete() {
  return { type: actions.ACTIVE_ROOMS_COMPLETE };
}

export function resetActiveRoomsList() {
  return { type: actions.ACTIVE_ROOMS_RESET };
}

export function handleMoreActiveRooms() {
  return (dispatch, getState) => {
    const limit = 10;
    const offset = findLastRank(getState());

    dispatch(loadingMoreActiveRooms());
    API.fetchMoreActiveRooms(limit, offset)
      .then(response => {
        if (Object.keys(response.data).length < 10) {
          dispatch(setActiveRoomsComplete());
        }
        dispatch(loadedMoreActiveRooms(response.data));
      }).catch(error => {
        dispatch(failedLoadingMoreActiveRooms(error));
      });
  };
}
