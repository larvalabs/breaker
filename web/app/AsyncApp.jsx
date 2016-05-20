import React, { Component, PropTypes } from 'react'
import { connect } from 'react-redux'

import Header from '../header/Header'
import Sidebar from '../sidebar/Sidebar'
import Main from './Main'
import Immutable from 'immutable'
import ChatDocumentTitle from './ChatDocumentTitle'
import Config from '../config'

class AsyncApp extends Component {
  render(){
    const { roomName ,
            rooms, room, unreadCount,
            sidebarOpen } = this.props;
    return (
      <ChatDocumentTitle unreadCount={unreadCount} roomName={roomName}>
        <div className={`app app-header-fixed app-aside-fixed ${this.props.roomName}`}>
          <Header unreadCount={unreadCount}/>
          <Sidebar roomList={rooms}
                   roomName={roomName}
                   open={sidebarOpen}
                   room={room} />
          <Main />
        </div>
      </ChatDocumentTitle>
    );
  }
}

function mapStateToProps(state) {
  let roomName = state.get('currentRoom');
  let members = state.getIn(['members']);
  let rooms = state.get('rooms');
  let lastReadTimes = state.get('lastSeenTimes');

  if(Config.guest){
    rooms = rooms.filter(room => room.get('name') === roomName);
    lastReadTimes = lastReadTimes.filter((_, currRoomName) => currRoomName === roomName)
  }


  let unreadCount = lastReadTimes.reduce((total, lastReadTime, roomName) => {
    return total + state.getIn(['roomMessages', roomName]).reduce((total, messageId) => {
          let messageTime = state.getIn(['messages', messageId, 'createDateLongUTC']);
          return messageTime && messageTime - lastReadTime > 0 ? total + 1 : total;
        }, 0);
  }, 0);

  return {
    roomName: roomName,
    rooms: rooms,
    room: state.getIn(['rooms', roomName], Immutable.Map()),
    sidebarOpen: state.getIn(['ui', 'sidebar_open']),
    settingsOpen: state.getIn(['ui', 'settings_open']),
    unreadCount
  }
}

export default connect(mapStateToProps)(AsyncApp)
