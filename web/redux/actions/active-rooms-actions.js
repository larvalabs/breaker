import * as actions from '../constants/active-rooms-contants';

export function loadingMoreActiveRooms() {
    return { type: actions.ACTIVE_ROOMS_LOADING_MORE };
}

export function loadedMoreActiveRooms(activeRooms) {
    return { type: actions.ACTIVE_ROOMS_LOADED_MORE, activeRooms };
}

export function failedLoadingMoreActiveRooms(error) {
    return null;
}

export function handleMoreActiveRooms() {
    return (dispatch, getState) => {
        const state = getState();
        const limit = 10;
        const offset = 5;

        dispatch(loadingMoreActiveRooms());
        API.fetchMoreActiveRooms(limit, offset)
            .then(response => {
               dispatch(loadedMoreActiveRooms(response.data));
            }).catch(error => {
                dispatch(failedLoadingMoreActiveRooms(error));
            });
    }
}