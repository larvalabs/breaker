import React, { Component } from 'react';
import { connect } from 'react-redux';
import Immutable from 'immutable';

import sort from '../util/sort';

import UserListItems from './UserListItems';
import UserListScrollable from './UserListScrollable';


class UserListBox extends Component {
  render() {
    const { mods, online, offline, roomName } = this.props;
    return (
      <UserListScrollable>
        <UserListItems items={mods} roomName={roomName} title="Mods">No mods online</UserListItems>
        <UserListItems items={online} roomName={roomName} title="Here Now">No users online</UserListItems>
        <UserListItems items={offline} roomName={roomName} title="Offline">No users offline</UserListItems>
      </UserListScrollable>
    );
  }
}

UserListBox.defaultProps = {
  online: Immutable.Map(),
  offline: Immutable.Map(),
  mods: Immutable.Map(),
  roomName: ''
};

function mapStateToProps(state) {
  const roomName = state.get('currentRoom');
  const users = state.get('users');
  const roomMembers = state.getIn(['members', roomName], Immutable.Map());

  const roomModUserNames = state.getIn(['rooms', roomName, 'moderators'], Immutable.List());
  const modUsers = roomModUserNames.map((member) => users.get(member));

  const roomOnlineUserNames = roomMembers.get('online', Immutable.List());
  const onlineUsers = roomOnlineUserNames.subtract(roomModUserNames).map((member) => users.get(member));

  const roomOfflineUserNames = roomMembers.get('offline', Immutable.List()).filter(u => u !== 'guest');
  const offlineUsers = roomOfflineUserNames.subtract(roomModUserNames).map((member) => users.get(member));

  return {
    online: onlineUsers.sort(sort.usersAlphabetical),
    mods: modUsers.sort(sort.usersAlphabetical),
    offline: offlineUsers.sort(sort.usersAlphabetical),
    roomName
  };
}

export default connect(mapStateToProps)(UserListBox);
