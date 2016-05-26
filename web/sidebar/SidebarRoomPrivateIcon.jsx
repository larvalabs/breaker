import React, { Component } from 'react';
import Immutable from 'immutable';


export default class SidebarRoomPrivateIcon extends Component {
  render() {
    const { room, styles } = this.props;

    if (!room.get('isPrivate')) {
      return null;
    }

    const iconStyles = {
      color: styles.get('sidebarTextColor')
    };

    return <i style={iconStyles} className="fa fa-lock private-icon" />;
  }
}

SidebarRoomPrivateIcon.defaultProps = {
  room: Immutable.Map(),
  styles: Immutable.Map()
};
