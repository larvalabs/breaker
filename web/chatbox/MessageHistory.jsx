import React, {Component} from 'react';
import { connect } from 'react-redux';
import Immutable from 'immutable';

import * as scrollActions from '../redux/actions/scroll-actions';


export default class MessageHistory extends Component {
  renderTitle() {
    if (this.props.loading) {
      return <span className="text-muted">Getting history...</span>;
    }

    if (!this.props.hasMore) {
      return <span className="text-muted">This is the beginning of /r/{this.props.currentRoom}</span>;
    }

    return <a className="more" onClick={this.props.handleMoreMessages}>More</a>;
  }

  render() {
    if (this.props.messageCount < 20) {
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
