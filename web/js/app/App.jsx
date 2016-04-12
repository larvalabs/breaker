import React, { Component, PropTypes } from 'react'
import { Provider } from 'react-redux';

import configureStore from '../redux/store/configureStore.js';
import AsyncApp from './AsyncApp.jsx';
import DevTools from './DevTools.js'

const store = configureStore(window.__INITIAL_STATE__);

export default class App extends Component {
  render() {
    return (
        <Provider store={store}>
          <div>
            <AsyncApp />
            <DevTools />
          </div>
        </Provider>
    );
  };
}
