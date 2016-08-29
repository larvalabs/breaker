import React, { Component } from 'react';
import { connect } from 'react-redux';

import * as scrollActions from '../redux/actions/scroll-actions';
import { getMoreMessagesLoading } from '../redux/selectors/ui-selectors';
import { getCurrentRoomHasMoreMessages, getMessageCountCurrentRoom } from '../redux/selectors/room-messages-selectors';

export class MessageHistory extends Component {
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
  currentRoom: '',
  messageCount: 0,
  loading: false,
  hasMore: true,
  handleMoreMessages: () => {}
};

function mapStateToProps(state) {
  return {
    currentRoom: state.get('currentRoom'),
    loading: getMoreMessagesLoading(state),
    hasMore: getCurrentRoomHasMoreMessages(state),
    messageCount: getMessageCountCurrentRoom(state)
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
