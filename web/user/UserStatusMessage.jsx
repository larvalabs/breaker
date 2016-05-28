import React, { Component } from 'react';
import Immutable from 'immutable';

import Config from '../config';


export default class UserStatusMessage extends Component {
  render() {
    const { user, roomName } = this.props;

    let statusMessage = user.get('statusMessage') ? user.get('statusMessage') : null;
    if (Config.features.useFlairStyle(roomName)) {
      statusMessage = user.getIn(['flair', roomName, 'flairText']);
    }

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
  roomName: ''
};
