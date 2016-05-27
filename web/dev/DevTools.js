import React from 'react';
import { createDevTools } from 'redux-devtools';
import LogMonitor from 'redux-devtools-log-monitor';
import DockMonitor from 'redux-devtools-dock-monitor';
import Dispatcher from 'redux-devtools-dispatch';
import MultipleMonitors from 'redux-devtools-multiple-monitors';
import FilterMonitor from 'redux-devtools-filter-actions';

import * as scrollActions from '../redux/actions/scroll-actions';
import * as chatActions from '../redux/actions/chat-actions';
import * as socketActions from '../redux/actions/socket-actions';
import * as notifyActions from '../redux/actions/notification-actions';
import * as chatConstants from '../redux/constants/chat-constants';
import * as socketConstants from '../redux/constants/socket-constants';
import * as menuConstants from '../redux/constants/menu-constants';

const actionCreators = {
  chat: chatActions,
  socket: socketActions,
  scroll: scrollActions,
  notify: notifyActions
};

export default createDevTools(
    <DockMonitor
        toggleVisibilityKey="ctrl-h"
        changePositionKey="ctrl-w"
        defaultIsVisible={false}
    >
      <FilterMonitor blacklist={[
        chatConstants.CHAT_BLURRED,
        chatConstants.CHAT_FOCUSED,
        chatConstants.CHAT_SET_INPUT_FOCUS,
        chatConstants.CHAT_RESET_INPUT_FOCUS,
        socketConstants.SOCK_UPDATE_LAST_READ,
        menuConstants.UI_SIDEBAR_CLOSE]}
      >
      <MultipleMonitors>
        <LogMonitor />
        <Dispatcher actionCreators={actionCreators} />
      </MultipleMonitors>
    </FilterMonitor>
    </DockMonitor>
);
