import React, { Component } from 'react';
import Immutable from 'immutable';

import UserKarma from './UserKarmaAndJoin';
import UserStatusMessage from './UserStatusMessage';


export default class UserDescription extends Component {
  render() {
    const { user } = this.props;

    return (
      <div className="text-muted description">
        <UserKarma user={user} />
        <UserStatusMessage user={user} />
      </div>
    );
  }
}

UserDescription.defaultProps = {
  user: Immutable.Map(),
};
