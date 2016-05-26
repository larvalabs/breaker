import React, { Component } from 'react';
import Immutable from 'immutable';
import TimeAgo from 'react-timeago';

import UsernameAndFlair from '../userlist/UsernameAndFlair.jsx';
import UserAvatar from '../userlist/UserAvatar';
import Message from './Message';


export default class ChatMessage extends Component {
  renderTime() {
    if (!this.props.message.get('createDateLongUTC')) {
      return null;
    }

    return (
      <div className="pull-right text-sm hidden-xs text-muted">
        <TimeAgo date={new Date(this.props.message.get('createDateLongUTC')).toISOString()} />
      </div>
    );
  }

  render() {
    const liClasses = 'chat-message-root-old list-group-item no-border p-t-s p-b-xs clearfix b-l-3x b-l-white';

    return (
      <li className={liClasses}>
        <UserAvatar user={this.props.user} roomName={this.props.roomName} />
        {this.renderTime()}
        <div className="clear">
          <UsernameAndFlair user={this.props.user} roomName={this.props.roomName}
                            messageUsername={this.props.message.get('username')}
          />
          <Message message={this.props.message} />
        </div>

      </li>
    );
  }
}

ChatMessage.defaultProps = {
  message: Immutable.Map(),
  user: Immutable.Map(),
  root: true
};
