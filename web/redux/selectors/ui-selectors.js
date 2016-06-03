import { createSelector } from 'reselect'


const getUi = (state) => state.get('ui');

export const getSidebarOpen = createSelector(
    [getUi],
    (ui) => {
      return ui.get('sidebar_open', false);
    }
);

export const getSettingsOpen = createSelector(
    [getUi],
    (ui) => {
      return ui.get('settings_open', false);
    }
);

export const getScrollToRoomName = createSelector(
    [getUi],
    (ui) => {
      return ui.get('scrollToRoomName');
    }
);

export const getMoreMessagesLoading = createSelector(
    [getUi],
    (ui) => {
      return ui.get('moreMessagesLoading', false);
    }
);

export const getConnected = createSelector(
    [getUi],
    (ui) => {
      return ui.get('connected', false);
    }
);

export const getSetInputFocus = createSelector(
    [getUi],
    (ui) => {
      return ui.get('setInputFocus', false);
    }
);
