import { createSelector } from 'reselect';
import Immutable from 'immutable';


const getUi = (state) => state.get('ui');
const getCollapsedLinks = (state) => state.getIn(['ui', 'collapsedLinks'], Immutable.List());

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


export const getSettingsOrSidebarOpen = createSelector(
    [getSidebarOpen, getSettingsOpen],
    (sidebarOpen, settingsOpen) => {
      return sidebarOpen || settingsOpen;
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

export const getMoreActiveRoomsLoading = createSelector(
  [getUi],
  (ui) => {
    return ui.get('moreActiveRoomsLoading', false);
  }
);

export const isActiveRoomsComplete = createSelector(
  [getUi],
  (ui) => {
    return ui.get('activeRoomsComplete', false);
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

export const getScrollToMessageId = createSelector(
    [getUi],
    (ui) => {
      return ui.get('scrollToMessageId', null);
    }
);


export const getAllCollapsedLinks = createSelector(
    [getCollapsedLinks],
    (links) => {
      return links;
    }
);
