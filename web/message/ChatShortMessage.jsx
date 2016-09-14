import React, { Component } from 'react';
import Immutable from 'immutable';

import Config from '../config';

import Clear from '../util/Clear';
import Message from './Message';


export default class ChatMessage extends Component {
  renderUserImage() {
    return (
      <a className="avatar thumb-sm pull-left m-r hidden-xs"
         href={`https://reddit.com/u/${this.props.user.get('username')}`} target="_blank"
      >
        &nbsp;
      </a>
    );
  }

  render() {
    const liClasses = 'chat-message-short list-group-item p-t-none p-b-xs no-border clearfix b-l-3x b-l-white';

    return (
      <li className={liClasses}>
        {this.renderUserImage()}
        <Clear>
          <Message message={this.props.message} userIsMod={this.props.userIsMod} />
        </Clear>
      </li>
    );
  }
}

ChatMessage.defaultProps = {
  message: Immutable.Map(),
  user: Immutable.Map()
};
