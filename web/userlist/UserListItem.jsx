import React, { Component } from 'react';
import Immutable from 'immutable';

import UsernameAndFlair from './UsernameAndFlair';
import UserAvatar from './UserAvatar';
import UserStatusMessage from './UserStatusMessage';
import Clear from '../util/Clear';


export default class UserListItem extends Component {
  render() {
    if (this.props.user.get('username') === 'guest') {
      return null;
    }

    return (
      <li key={this.props.user.get('id')} className="list-group-item user-list-item">
        <UserAvatar user={this.props.user} roomName={this.props.roomName}/>
        <Clear>
          <UsernameAndFlair user={this.props.user} roomName={this.props.roomName}
                            classOnly={this.props.roomName === 'nba'} noFlair
          />
          <UserStatusMessage user={this.props.user}
                             roomName={this.props.roomName}
          />
        </Clear>
      </li>
    );
  }
}

UserListItem.defaultProps = {
  user: Immutable.Map(),
  roomName: ''
};
