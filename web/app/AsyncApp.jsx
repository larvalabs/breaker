import React, { Component, PropTypes } from 'react';
import { connect } from 'react-redux';
import Immutable from 'immutable';

import Header from '../header/Header';
import Sidebar from '../sidebar/Sidebar';
import Main from './Main';
import DocumentTitle from '../document/DocumentTitle';
import Notifications from '../notifications/Notifications';

import { scrollToRoomNameReset } from '../redux/actions/scroll-actions';
import { getTotalLastSeenTimes } from '../redux/selectors/last-seen-selectors'
import { getSettingsOpen, getSidebarOpen, getScrollToRoomName } from '../redux/selectors/ui-selectors'
import { getAllRooms, getCurrentRoom, getCurrentRoomStyles } from '../redux/selectors/rooms-selectors'

class AsyncApp extends Component {
  render() {
    const { roomName,
            rooms, room, unreadCount,
            sidebarOpen, resetScrollToRoomName,
            scrollToRoomName, sidebarStyles } = this.props;
    return (
      <DocumentTitle unreadCount={unreadCount} roomName={roomName}>

        <div className={`app app-header-fixed app-aside-fixed ${this.props.roomName}`}>
          <Notifications />
          <Header unreadCount={unreadCount}/>
          <Sidebar roomList={rooms}
                   roomName={roomName}
                   open={sidebarOpen}
                   room={room}
                   scrollToRoomNameReset={resetScrollToRoomName}
                   scrollToRoomName={scrollToRoomName} styles={sidebarStyles}
          />
          <Main />
        </div>
      </DocumentTitle>
    );
  }
}

function mapStateToProps(state) {
  return {
    roomName: state.get('currentRoom'),
    sidebarStyles: getCurrentRoomStyles(state),
    rooms: getAllRooms(state),
    room: getCurrentRoom(state),
    sidebarOpen: getSidebarOpen(state),
    settingsOpen: getSettingsOpen(state),
    unreadCount: getTotalLastSeenTimes(state),
    scrollToRoomName: getScrollToRoomName(state)
  };
}

function mapDispatchToProps(dispatch) {
  return {
    resetScrollToRoomName() {
      dispatch(scrollToRoomNameReset());
    }
  };
}

export default connect(mapStateToProps, mapDispatchToProps)(AsyncApp);
