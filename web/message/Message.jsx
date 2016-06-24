import React, { Component } from 'react';
import { connect } from 'react-redux';
import Immutable from 'immutable';

import Config from '../config';

import LinkInfo from '../links/LinkInfo';


class Message extends Component {
  constructor(props) {
    super(props);
    this.renderFormattedMention = this.renderFormattedMention.bind(this);
  }

  renderFormattedMention(match, username) {
    if (this.props.message.get('mentionedUsernames', Immutable.List()).contains(username.toLowerCase())) {
      let classes = 'username';
      if (username.toLowerCase() === this.props.authUser.get('username')) {
        classes += ' username-me';
      }
      return `<a target="_blank" class="${classes}" href=${`https://reddit.com/u/${username}`}>${match}</a>`;
    }

    return username;
  }

  renderRawMessage() {
    return (
      <div className="message-body m-t-midxs">
        <i>{this.props.message.get('message')}</i>
      </div>
    );
  }

  renderHTMLMessage() {
    let classes = 'message-body m-t-midxs';
    const message = this.props.message.get('messageHtml').replace(/@([A-Za-z0-9_-]+)/g, this.renderFormattedMention);
    return <div className={classes} dangerouslySetInnerHTML={ { __html: message } }></div>;
  }

  renderMessageBody() {
    if (this.props.message.get('messageHtml')) {
      return this.renderHTMLMessage();
    }
    return this.renderRawMessage();
  }

  render() {
    return (
      <div>
        {this.renderMessageBody()}
        <LinkInfo linkInfo={this.props.message.getIn(['linkInfo', 0])} uuid={this.props.message.get('uuid')} />
      </div>
    );
  }
}

Message.defaultProps = {
  message: Immutable.Map(),
  user: Immutable.Map(),
  root: true
};

function mapStateToProps(state) {
  return {
    authUser: state.get('authUser')
  };
}

export default connect(mapStateToProps)(Message);
