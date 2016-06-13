import API from '../../api';

import * as actions from '../constants/active-rooms-contants';


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

export function loadMoreActiveRooms() {
  const findLastRank = (activeRoomsState) => {
    let rank = 0;
    activeRoomsState.toArray().forEach(room => {
      if (rank < room.get('rank')) rank = room.get('rank');
    });
    return rank;
  };

  return (dispatch, getState) => {
    const state = getState().get('activeRooms');
    const limit = 10;
    const offset = findLastRank(state);

    dispatch(loadingMoreActiveRooms());
    API.fetchMoreActiveRooms(limit, offset)
      .then(response => {
        if (response.data.length <= 10) dispatch(setActiveRoomsComplete());
        dispatch(loadedMoreActiveRooms(response.data));
      }).catch(error => {
        dispatch(failedLoadingMoreActiveRooms(error));
      });
  };
}
