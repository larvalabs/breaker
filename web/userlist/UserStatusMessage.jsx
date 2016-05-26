import React, { Component } from 'react';
import Config from '../config';


export default class UserStatusMessage extends Component {
  render() {
    if (Config.features.useFlairStyle(this.props.roomName)) {
      const flairText = this.props.user.getIn(['flair', this.props.roomName, 'flairText']);
      return <small className="text-muted">{flairText}</small>;
    }
    const statusMessage = this.props.user.get('statusMessage') ? this.props.user.get('statusMessage') : '\u00a0';
    return <small className="text-muted">{statusMessage}</small>;
  }
}
