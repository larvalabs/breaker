import React, { Component } from "react";
import { connect } from 'react-redux';
import Immutable from 'immutable';

import RoomIcon from './RoomIcon';
import RoomTitleInfo from './RoomTitleInfo';
import HeaderUserMenu from './HeaderUserMenu';
import HeaderLogoMenuBox from './HeaderLogoMenuBox';


class Header extends Component {
  render() {
    let classes = 'collapse pos-rlt navbar-collapse box-shadow bg-white-only';
    if (this.props.settings_open) {
      classes += ' show';
    }

    return (
      <header className="app-header navbar" role="menu">
        <HeaderLogoMenuBox room={this.props.room} unreadCount={this.props.unreadCount}
                           sidebar_open={this.props.sidebar_open} settings_open={this.props.settings_open}
        />
        <div className={classes}>
          <RoomIcon room={this.props.room} />
          <RoomTitleInfo room={this.props.room} userIsMod={this.props.userIsMod}/>
          <HeaderUserMenu room={this.props.room} user={this.props.user}/>
        </div>
      </header>
    );
  }
}

Header.defaultProps = {
  user: Immutable.Map(),
  room: Immutable.Map(),
  roomName: null,
  userIsMod: false,
  settings_open: false,
  sidebar_open: false
};

function mapStateToProps(state) {
  const user = state.get('authUser');
  const roomName = state.get('currentRoom');
  const members = state.getIn(['members']);

  let userIsMod;
  if (members && members.get('breakerapp')) {
    userIsMod = !!state.getIn(['members', roomName, 'mods', user.get('username')]);
  }

  return {
    user,
    userIsMod,
    roomName,
    room: state.getIn(['rooms', roomName], Immutable.Map()),
    sidebar_open: state.getIn(['ui', 'sidebar_open'], false),
    settings_open: state.getIn(['ui', 'settings_open'], false)
  };
}

export default connect(mapStateToProps)(Header);
