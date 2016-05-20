import React from 'react';
import { createDevTools } from 'redux-devtools';
import LogMonitor from 'redux-devtools-log-monitor';
import DockMonitor from 'redux-devtools-dock-monitor';
import Dispatcher from 'redux-devtools-dispatch';
import MultipleMonitors from 'redux-devtools-multiple-monitors';

import * as scrollActions from '../redux/actions/scroll-actions'
import * as chatActions from '../redux/actions/chat-actions'
import * as socketActions from '../redux/actions/socket-actions'

const actionCreators = {
  chat : chatActions,
  socket: socketActions,
  scroll: scrollActions
};

export default createDevTools(
    <DockMonitor
        toggleVisibilityKey="ctrl-h"
        changePositionKey="ctrl-w"
        defaultIsVisible={false}
    >
      <MultipleMonitors>
        <LogMonitor />
        <Dispatcher actionCreators={actionCreators} />
      </MultipleMonitors>
    </DockMonitor>
);
