import React, { Component, PropTypes } from 'react'
import { Provider } from 'react-redux';

import store from '../redux/store/store';
import AsyncApp from './AsyncApp';
import DevTools from './DevTools'
import Config from '../config'

export default class App extends Component {
  render() {
    if(Config.environment.prod){
      return (
          <Provider store={store}>
            <div>
              <AsyncApp/>
            </div>
          </Provider>
      );
    } else {
      
      // Include DevTools
      return (
          <Provider store={store}>
            <div>
              <AsyncApp />
              <DevTools />
            </div>
          </Provider>
      );
    }
    
  };
}
