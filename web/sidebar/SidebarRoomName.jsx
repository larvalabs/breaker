import React, { Component } from 'react';
import Immutable from 'immutable';

import SidebarRoomPrivateIcon from './SidebarRoomPrivateIcon';

export default class SidebarRoomName extends Component {
  renderLockOrHash() {
    const { room, styles } = this.props;

    if (!room.get('isPrivate')) {
      return '#';
    }
    return <SidebarRoomPrivateIcon styles={styles} />
  }

  render() {
    const { room, styles, active } = this.props;

    const roomNameStyles = {
      color: styles.get('sidebarTextColor'),
      marginLeft: '2px',
      verticalAligh: 'middle',
      lineHeight: '30px'
    };

    return (
        <span className={`roomname ${active ? 'active' : ''}`} style={roomNameStyles}>
          {this.renderLockOrHash()}{room.get('displayName')}
      </span>
    );
  }
}

SidebarRoomName.defaultProps = {
  styles: Immutable.Map(),
  room: Immutable.Map(),
  active: false
};
