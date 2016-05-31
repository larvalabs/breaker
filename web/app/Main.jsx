import React, { Component } from 'react';

import Chat from '../chat/Chat';
import UserList from '../userlist/UserList';
import MobileRoomOverlay from '../mobile/MobileRoomOverlay';


export default class Main extends Component {
  render() {
    return (
      <div id="content" className="app-content" role="main">
        <div className="app-content-body app-content-full h-full">
          <MobileRoomOverlay />
          <div className="hbox bg-light ">
            <Chat />
            <UserList />
          </div>
        </div>
      </div>
    );
  }
}
