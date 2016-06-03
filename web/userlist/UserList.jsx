import React, { Component } from 'react';
import { connect } from 'react-redux';
import Immutable from 'immutable';

import sort from '../util/sort';

import UserListItems from './UserListItems';
import UserListScrollable from './UserListScrollable';

import { 
    getModUsersForCurrentRoom,
    getOnlineUsersForCurrentRoom,
    getOfflineUsersForCurrentRoom
} from '../redux/selectors/users-selectors';


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
  return {
    roomName: state.get('currentRoom'),
    online: getOnlineUsersForCurrentRoom(state),
    mods: getModUsersForCurrentRoom(state),
    offline: getOfflineUsersForCurrentRoom(state)
  };
}

export default connect(mapStateToProps)(UserListBox);
