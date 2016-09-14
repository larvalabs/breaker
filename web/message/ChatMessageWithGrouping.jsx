import React, { Component } from 'react';
import Immutable from 'immutable';

import Config from '../config';

import ChatShortMessage from '../message/ChatShortMessage';
import ChatMessage from '../message/ChatMessage';


export default class ChatMessageWithGrouping extends Component {
  isSentinel(message) {
    return message.get('type') === 'first_sentinel';
  }

  shouldRenderShortMessage(previousMessage, currentMessage) {
    if (!previousMessage) {
      return false;
    }

    const previousTime = previousMessage.get('createDateLongUTC');
    const currentTime = currentMessage.get('createDateLongUTC');

    const userIsTheSame = previousMessage.get('username') === currentMessage.get('username');
    const timeUnderAllowedBreak = currentTime - previousTime < Config.settings.message_split_millis;
    return userIsTheSame && timeUnderAllowedBreak;
  }

  render() {
    const { currentMessage, previousMessage, user } = this.props;

    if (this.isSentinel(currentMessage)) {
      return null;
    }

    if (this.shouldRenderShortMessage(previousMessage, currentMessage)) {
      return <ChatShortMessage message={currentMessage} user={user} userIsMod={this.props.userIsMod} />;
    }

    return <ChatMessage message={currentMessage} user={user} userIsMod={this.props.userIsMod} />;
  }
}

ChatMessageWithGrouping.defaultProps = {
  user: Immutable.Map(),
  currentMessage: Immutable.Map(),
  previousMessage: Immutable.Map(),
  userIsMod: false
};
