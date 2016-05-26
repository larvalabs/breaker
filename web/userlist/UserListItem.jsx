import React, { Component } from 'react';
import Immutable from 'immutable';

import UsernameAndFlair from '../user/UsernameAndFlair';
import UserAvatar from '../user/UserAvatar';
import UserStatusMessage from '../user/UserStatusMessage';
import Clear from '../util/Clear';


export default class UserListItem extends Component {
  render() {
    const { user, roomName } = this.props;

    if (user.get('username') === 'guest') {
      return null;
    }

    return (
      <li className="list-group-item user-list-item">
        <UserAvatar user={user} roomName={roomName}/>
        <Clear>
          <UsernameAndFlair user={user} roomName={roomName} classOnly={roomName === 'nba'} noFlair />
          <UserStatusMessage user={user} roomName={roomName} />
        </Clear>
      </li>
    );
  }
}

UserListItem.defaultProps = {
  user: Immutable.Map(),
  roomName: ''
};
