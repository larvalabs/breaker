import React, { Component } from 'react';
import Immutable from 'immutable';


export default class SidebarRoomName extends Component {
  render() {
    const { room, styles, active } = this.props;

    const roomNameStyles = {
      color: styles.get('sidebarTextColor'),
      marginLeft: '2px'
    };

    return (
        <span className={`roomname ${active ? 'active' : ''}`} style={roomNameStyles}>
        #{room.get('displayName')}
      </span>
    );
  }
}

SidebarRoomName.defaultProps = {
  styles: Immutable.Map(),
  room: Immutable.Map(),
  active: false
};
