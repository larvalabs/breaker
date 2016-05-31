import React, { Component } from 'react';
import { connect } from 'react-redux';
import Immutable from 'immutable';

import * as scrollActions from '../redux/actions/scroll-actions';


export default class MessageHistory extends Component {
  renderTitle() {
    const { loading, currentRoom, hasMore, handleMoreMessages } = this.props;
    if (loading) {
      return <span className="text-muted">Getting history...</span>;
    }

    if (!hasMore) {
      return <span className="text-muted">This is the beginning of /r/{currentRoom}</span>;
    }

    return <a className="more" onClick={handleMoreMessages}>More</a>;
  }

  render() {
    const { messageCount } = this.props;

    if (messageCount < 20) {
      return null;
    }

    return (
      <li className="message-fetch-message">
        {this.renderTitle()}
        <div className="divider"></div>
      </li>
    );
  }
}

MessageHistory.defaultProps = {
  message_count: 0,
  loading: false,
  hasMore: true,
  currentRoom: '',
  handleMoreMessages: () => {}
};

function mapStateToProps(state) {
  const roomMessages = state.getIn(['roomMessages', state.get('currentRoom')], Immutable.List());
  const firstMessage = state.getIn(['messages', roomMessages.first()]);
  const messageCount = roomMessages.size;

  let hasMore = false;
  if (firstMessage) {
    hasMore = firstMessage.get('type', '') !== 'first_sentinel';
  }

  return {
    loading: state.getIn(['ui', 'moreMessagesLoading']),
    currentRoom: state.get('currentRoom'),
    hasMore,
    messageCount
  };
}

function mapDispatchToProps(dispatch) {
  return {
    handleMoreMessages() {
      dispatch(scrollActions.handleMoreMessages());
    }
  };
}

export default connect(mapStateToProps, mapDispatchToProps)(MessageHistory);
