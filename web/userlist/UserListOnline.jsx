import React, { Component } from 'react';
import Immutable from 'immutable';

import UserListItems from './UserListItem.jsx';

export default class UserListOnline extends Component {
  render() {
    return (
      <ul id="modlist" className="list-group no-bg no-borders pull-in m-b-sm">
        <UserListItems items={this.props.mods} roomName={this.props.roomName} />
      </ul>
    );
  }
}

UserListOnline.defaultProps = {
  mods: Immutable.List(),
  roomName: ''
};

