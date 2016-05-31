import React, { Component } from 'react';
import Immutable from 'immutable';


export default class UserStatusMessage extends Component {
  render() {
    const { user } = this.props;

    const statusMessage = user.get('statusMessage') ? user.get('statusMessage') : null;
    if (!statusMessage) {
      return null;
    }

    return (
        <span className="status-message" style={{ fontSize: '.9em' }}>{statusMessage}</span>
    );
  }
}

UserStatusMessage.defaultProps = {
  user: Immutable.Map(),
};
