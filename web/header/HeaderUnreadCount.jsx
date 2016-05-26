import React, { Component } from 'react';
import { connect } from 'react-redux';
import Immutable from 'immutable';

import { handleScrollToNextUnread } from '../redux/actions/scroll-actions';


export default class HeaderUnreadCount extends Component {
  render() {
    const backgroundColor = this.props.room.getIn(['styles', 'sidebarUnreadColor']);
    const color = this.props.room.getIn(['styles', 'sidebarUnreadTextColor']);
    const unreadStyles = {
      cursor: 'pointer',
      backgroundColor,
      color
    };

    if (this.props.unreadCount < 1) {
      return null;
    }

    return (
      <div className="unread-count-total pull-right">
        <b className="unreadcount label bg-info pull-right"
           title="Jump to next unread room"
           style={unreadStyles}
           onClick={this.props.scrollToUnreadRoom}
        >
          {this.props.unreadCount}
        </b>
      </div>
    );
  }
}

HeaderUnreadCount.defaultProps = {
  room: Immutable.Map(),
  unreadCount: 0
};

function mapDispatchToProps(dispatch) {
  return {
    scrollToUnreadRoom() {
      dispatch(handleScrollToNextUnread());
    }
  };
}

export default connect(null, mapDispatchToProps)(HeaderUnreadCount);
