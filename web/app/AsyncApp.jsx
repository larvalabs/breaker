import React, { Component, PropTypes } from 'react';
import { connect } from 'react-redux';

import Header from '../header/Header';
import Sidebar from '../sidebar/Sidebar';
import Main from './Main';
import DocumentTitle from '../document/DocumentTitle';
import Notifications from '../notifications/Notifications';


class AsyncApp extends Component {
  render() {
    const { roomName } = this.props;
    return (
      <DocumentTitle>
        <div className={`app app-header-fixed app-aside-fixed ${roomName}`}>
          <Notifications />
          <Header />
          <Sidebar/>
          <Main />
        </div>
      </DocumentTitle>
    );
  }
}

function mapStateToProps(state) {
  return {
    roomName: state.get('currentRoom')
  };
}

export default connect(mapStateToProps)(AsyncApp);
