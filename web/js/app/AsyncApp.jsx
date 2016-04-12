import React, { Component, PropTypes } from 'react'
import { connect } from 'react-redux'

import Header from './Header.jsx'
import Sidebar from './Sidebar.jsx'
import Main from './Main.jsx'

class AsyncApp extends Component {
  render(){
    const { user, activeRooms, roomName } = this.props;
    return (
      <div className="app app-header-fixed app-aside-fixed">
        <Header user={user} roomName={roomName}/>
        <Sidebar activeRooms={ActiveRooms}/>
        <Main />
      </div>
    );
  }
}

function mapStateToProps(state) {
  console.log("state", state);
  return {
    user: state.user,
    activeRooms: state.activeRooms,
    roomName: state.roomName
  }
}

export default connect(mapStateToProps)(AsyncApp)
