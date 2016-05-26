import React, { Component } from 'react';
import Immutable from 'immutable';


export default class SidebarRoomAvatar extends Component {
  render() {
    const { room } = this.props;

    let roomIcon = room.get('iconUrl', null);
    if (!roomIcon) {
      roomIcon = '/public/images/blank.png';
    }

    return <img className="roomIconSmall" src={roomIcon}/>;
  }
}

SidebarRoomAvatar.defaultProps = {
  room: Immutable.Map()
};
