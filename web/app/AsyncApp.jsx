import React, { Component, PropTypes } from 'react'
import { connect } from 'react-redux'

import Header from './Header'
import Sidebar from '../sidebar/Sidebar'
import Main from './Main'
import Immutable from 'immutable'
import ChatDocumentTitle from './ChatDocumentTitle'

class AsyncApp extends Component {
  render(){
    const { user, activeRooms, roomName , rooms, room, userIsMod, unreadCounts} = this.props;
    return (
      <ChatDocumentTitle>
        <div className={`app app-header-fixed app-aside-fixed ${this.props.roomName}`}>
          <Header user={user} roomName={roomName} room={room} userIsMod={userIsMod}/>
          <Sidebar activeRooms={activeRooms} roomList={rooms} roomName={roomName} unreadCounts={unreadCounts}/>
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

  let userIsMod;
  if (members && members.get('breakerapp')) {
    userIsMod = !!state.getIn(['members', roomName, 'mods', user.get('username')]);
  }
  return {
    user: user,
    userIsMod: userIsMod,
    activeRooms: state.getIn(['initial', 'activeRooms']),
    roomName: roomName,
    rooms: state.get('rooms'),
    room: state.getIn(['rooms', roomName], Immutable.Map()),
    unreadCounts: state.get('unreadCounts')
  }
}

export default connect(mapStateToProps)(AsyncApp)
