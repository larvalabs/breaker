import React, { Component } from 'react';
import Immutable from 'immutable';


export default class SidebarRoomPrivateIcon extends Component {
  render() {
    const { styles } = this.props;

    const iconStyles = {
      color: styles.get('sidebarTextColor')
    };

    return <i style={iconStyles} className="fa fa-lock private-icon" />;
  }
}

SidebarRoomPrivateIcon.defaultProps = {
  styles: Immutable.Map()
};
