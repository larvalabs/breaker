import React, { Component, PropTypes } from 'react'
import { Provider } from 'react-redux';

import store from '../redux/store/store.js';
import AsyncApp from './AsyncApp.jsx';
import DevTools from './DevTools.js'


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
