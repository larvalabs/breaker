import React, { Component } from 'react';
import { connect } from 'react-redux'
import Immutable from 'immutable';

import { makeGetLastSeenTimeForRoom } from '../redux/selectors/last-seen-selectors';


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

const makeMapStateToProps = () => {
  const getLastSeenTimeForRoom = makeGetLastSeenTimeForRoom();
  return (state, ownProps) => {
    return {
      unreadCount: getLastSeenTimeForRoom(state, ownProps)
    };
  };
};

export default connect(makeMapStateToProps)(SidebarRoomUnreadCount);
