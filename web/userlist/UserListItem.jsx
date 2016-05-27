import React, { Component } from 'react';
import Immutable from 'immutable';

import UsernameAndFlair from '../user/UsernameAndFlair';
import UserAvatar from '../user/UserAvatar';
import UserDescription from '../user/UserDescription';
import Clear from '../util/Clear';


export default class UserListItem extends Component {
  render() {
    const { user, roomName } = this.props;

    if (user.get('username') === 'guest') {
      return null;
    }

    return (
      <li className="list-group-item user-list-item" style={{ overflow: 'hidden' }}>
        <UserAvatar user={user} roomName={roomName}/>
        <Clear>
          <UsernameAndFlair user={user} roomName={roomName} classOnly={roomName === 'nba'} noFlair />
          <UserDescription user={user} roomName={roomName} />
        </Clear>
      </li>
    );
  }
}

UserListItem.defaultProps = {
  user: Immutable.Map(),
  roomName: ''
};
