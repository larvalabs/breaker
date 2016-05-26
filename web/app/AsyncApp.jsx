import React, { Component, PropTypes } from 'react';
import { connect } from 'react-redux';
import Immutable from 'immutable';

import Config from '../config';

import Header from '../header/Header';
import Sidebar from '../sidebar/Sidebar';
import Main from './Main';
import ChatDocumentTitle from './ChatDocumentTitle';
import Notifications from './Notifications';

import { scrollToRoomNameReset } from '../redux/actions/scroll-actions';


class AsyncApp extends Component {
  render() {
    const { roomName,
            rooms, room, unreadCount,
            sidebarOpen, resetScrollToRoomName,
            scrollToRoomName, sidebarStyles } = this.props;
    return (
      <ChatDocumentTitle unreadCount={unreadCount} roomName={roomName}>

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
      </ChatDocumentTitle>
    );
  }
}

function mapStateToProps(state) {
  const roomName = state.get('currentRoom');
  let rooms = state.get('rooms');
  let lastReadTimes = state.get('lastSeenTimes');

  if (Config.guest) {
    rooms = rooms.filter(room => room.get('name') === roomName);
    lastReadTimes = lastReadTimes.filter((_, currRoomName) => currRoomName === roomName)
  }

  const unreadCount = lastReadTimes.reduce((total, lastReadTime, currRoomName) => {
    return total + state.getIn(['roomMessages', currRoomName]).reduce((innerTotal, messageId) => {
      const messageTime = state.getIn(['messages', messageId, 'createDateLongUTC']);
      return messageTime && messageTime - lastReadTime > 0 ? innerTotal + 1 : innerTotal;
    }, 0);
  }, 0);

  const sidebarStyles = state.getIn(['rooms', state.get('currentRoom'), 'styles'], Immutable.Map());

  return {
    sidebarStyles,
    roomName,
    rooms,
    room: state.getIn(['rooms', roomName], Immutable.Map()),
    sidebarOpen: state.getIn(['ui', 'sidebar_open']),
    settingsOpen: state.getIn(['ui', 'settings_open']),
    unreadCount,
    scrollToRoomName: state.getIn(['ui', 'scrollToRoomName'])
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
