import React, { Component } from 'react';

import ChatBox from '../chatbox/ChatBox';
import UserListBox from '../userlist/UserList';
import MobileRoomOverlay from './MobileRoomOverlay';


export default class Main extends Component {
  render() {
    return (
      <div id="content" className="app-content" role="main">
        <div className="app-content-body app-content-full h-full">
          <MobileRoomOverlay />
          <div className="hbox bg-light ">
            <ChatBox />
            <UserListBox />
          </div>
        </div>
      </div>
    );
  }
}
