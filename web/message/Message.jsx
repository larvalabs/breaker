import React, { Component } from 'react';
import { connect } from 'react-redux';
import Immutable from 'immutable';

import Config from '../config';

import LinkInfo from '../links/LinkInfo';

import { handleDeleteMessage } from '../redux/actions/chat-actions';

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
    let classes = 'message-body m-t-midxs pos-rlt visible-on-hover-trigger';
    let deleteClasses = 'pull-right text-sm text-muted';
    let deleteHtml = '<div class="visible-on-parent-hover pull-right text-sm text-muted"><a href="#" onClick={this.props.handleDeleteMessage}>delete</a></div>';
    let message = this.props.message.get('messageHtml').replace(/@([A-Za-z0-9_-]+)/g, this.renderFormattedMention);
    if (this.props.message.get('deleted')) {
      message = '<i>[deleted]</i>';
    }
    if (this.props.userIsMod) {
      return <div className={classes}>
        <a className="visible-on-parent-hover" href="#" onClick={this.props.handleDeleteMessage}><i className="fa fa-trash text-muted pull-left text-xs m-t-xs"></i></a>
        <span dangerouslySetInnerHTML={ { __html: message } }/>
      </div>;
    } else {
      return <div className={classes}>
        <div dangerouslySetInnerHTML={ { __html: message } }/>
      </div>;
    }
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
  root: true,
  userIsMod: false
};

function mapStateToProps(state) {
  return {
    authUser: state.get('authUser')
  };
}

function mapDispatchToProps(dispatch, ownProps) {
  return {
    handleDeleteMessage() {
      dispatch(handleDeleteMessage(ownProps.message.get('uuid')));
    }
  };
}

export default connect(mapStateToProps, mapDispatchToProps)(Message);
