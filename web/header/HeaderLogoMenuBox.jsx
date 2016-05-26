import React, { Component } from 'react';
import Immutable from 'immutable';

import Config from '../config';

import HeaderAppLogo from './HeaderAppLogo';
import HeaderUnreadCount from './HeaderUnreadCount';
import MobileMenuButton from './MobileMenuButton';
import MobileSettingsButton from './MobileSettingsButton';


export default class RoomIcon extends Component {
  render() {
    const styles = Config.styles.getSidebarColorForRoom(this.props.room);

    return (
      <div className="navbar-header bg-dark" style={styles}>
        <HeaderUnreadCount room={this.props.room} unreadCount={this.props.unreadCount} />
        <MobileMenuButton room={this.props.room} sidebar_open={this.props.sidebar_open}/>
        <MobileSettingsButton room={this.props.room} settings_open={this.props.settings_open}/>
        <HeaderAppLogo room={this.props.room} />
      </div>
    );
  }
}

RoomIcon.defaultProps = {
  room: Immutable.Map(),
  unreadCount: 0,
  sidebar_open: false,
  settings_open: false
};
