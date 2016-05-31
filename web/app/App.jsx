import React, { Component, PropTypes } from 'react';
import { Provider } from 'react-redux';

import store from '../redux/store/store';
import Config from '../config';

import AsyncApp from './AsyncApp';
import DevTools from '../dev/DevTools';


export default class App extends Component {
  render() {
    if (Config.settings.dev_tools) {
      return (
          <Provider store={store}>
            <div>
              <AsyncApp />
              <DevTools />
            </div>
          </Provider>
      );
    }

    return (
        <Provider store={store}>
          <div>
            <AsyncApp/>
          </div>
        </Provider>
    );
  }
}
