import React, { Component, PropTypes } from 'react'
import { connect } from 'react-redux'

import Header from './Header'
import Sidebar from '../sidebar/Sidebar'
import Main from './Main'
import Immutable from 'immutable'
import ChatDocumentTitle from './ChatDocumentTitle'
import Config from '../config'

class AsyncApp extends Component {
  render(){
    const { user, activeRooms, roomName ,
            rooms, room, userIsMod, unreadCounts,
            sidebarOpen, settingsOpen } = this.props;
    return (
      <ChatDocumentTitle>
        <div className={`app app-header-fixed app-aside-fixed ${this.props.roomName}`}>
          <Header user={user} roomName={roomName} room={room} userIsMod={userIsMod}/>
          <Sidebar activeRooms={activeRooms}
                   roomList={rooms}
                   roomName={roomName}
                   unreadCounts={unreadCounts}
                   open={sidebarOpen}
                   room={room} />
          <Main />
        </div>
      </ChatDocumentTitle>
    );
  }
}

function mapStateToProps(state) {
  let roomName = state.getIn(['initial', 'roomName']);
  let user = state.getIn(['initial', 'user']);
  let members = state.getIn(['members']);
  let rooms = state.get('rooms');

  let userIsMod;
  if (members && members.get('breakerapp')) {
    userIsMod = !!state.getIn(['members', roomName, 'mods', user.get('username')]);
  }

  if(Config.guest){
    rooms = rooms.filter(room => room.get('name') === roomName);
  }

  return {
    user: user,
    userIsMod: userIsMod,
    activeRooms: state.getIn(['initial', 'activeRooms']),
    roomName: roomName,
    rooms: rooms,
    room: state.getIn(['rooms', roomName], Immutable.Map()),
    unreadCounts: state.get('unreadCounts'),
    sidebarOpen: state.getIn(['ui', 'sidebar_open']),
    settingsOpen: state.getIn(['ui', 'settings_open'])
  }
}

export default connect(mapStateToProps)(AsyncApp)
