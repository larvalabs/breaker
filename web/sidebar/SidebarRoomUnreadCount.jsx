import React, { Component } from 'react';
import { connect } from 'react-redux'
import Immutable from 'immutable';


class SidebarRoomUnreadCount extends Component {
  render() {
    const { unreadCount, styles } = this.props;

    if (unreadCount < 1) {
      return null;
    }

    const unreadCountStyles = {
      backgroundColor: styles.get('sidebarUnreadColor'),
      color: styles.get('sidebarUnreadTextColor')
    };

    return (
      <div className="unread-count-room pull-right">
        <b className="unreadcount label bg-info pull-right" style={unreadCountStyles}>{unreadCount}</b>
      </div>
    );
  }
}

SidebarRoomUnreadCount.defaultProps = {
  styles: Immutable.Map(),
  room: Immutable.Map(),
  unreadCount: 0
};

function mapStateToProps(state, ownProps) {
  const lastReadTime = state.getIn(['lastSeenTimes', ownProps.room.get('name')]);
  const unreadCount = state.getIn(['roomMessages', ownProps.room.get('name')]).reduce((total, messageId) => {
    const messageTime = state.getIn(['messages', messageId, 'createDateLongUTC']);
    return messageTime && messageTime - lastReadTime > 0 ? total + 1 : total;
  }, 0);

  return {
    unreadCount
  };
}

export default connect(mapStateToProps)(SidebarRoomUnreadCount);
