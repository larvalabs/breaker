import React, { Component } from 'react';
import { connect } from 'react-redux';
import Immutable from 'immutable';

import * as chatActions from '../redux/actions/chat-actions';


class SidebarRoomListElm extends Component {
  constructor(props) {
    super(props);
    this.onElementClicked = this.onElementClicked.bind(this);
    this.getStyles = this.getStyles.bind(this);
    this.onMouseEnter = this.onMouseEnter.bind(this);
    this.onMouseLeave = this.onMouseLeave.bind(this);
    this.state = {};
  }

  onElementClicked(event) {
    if (event.target.className.indexOf('close') < 0) {
      this.props.dispatch(chatActions.handleChangeRoom(this.props.room.get('name')));
    }
  }

  onMouseEnter() {
    this.setState({
      hover: true
    });
  }

  onMouseLeave() {
    this.setState({
      hover: false
    });
  }

  getStyles() {
    let backgroundColor = null;
    if (this.props.active) {
      backgroundColor = this.props.currentRoom.getIn(['styles', 'sidebarRoomSelectedColor']);
    } else if (this.state.hover) {
      backgroundColor = this.props.currentRoom.getIn(['styles', 'sidebarRoomHoverColor']);
    } else {
      return {};
    }

    return { backgroundColor };
  }

  renderUnreadCount(props) {
    const backgroundColor = this.props.currentRoom.getIn(['styles', 'sidebarUnreadColor']);
    const color = this.props.currentRoom.getIn(['styles', 'sidebarUnreadTextColor']);
    const unreadCountStyles = {
      backgroundColor,
      color
    };

    if (props.unreadCount > 0) {
      return (
        <div className="unread-count-room pull-right">
          <b className="unreadcount label bg-info pull-right"
             style={unreadCountStyles}
          >
            {this.props.unreadCount}
          </b>
        </div>
      );
    }

    return null;
  }

  renderPrivateIconIfNecessary(props) {
    if (!props.room.get('isPrivate')) {
      return null;
    }
    const iconColor = props.currentRoom.getIn(['styles', 'sidebarTextColor']);
    const iconStyles = { color: iconColor };

    return <i style={iconStyles} className="fa fa-lock private-icon" />;
  }

  render() {
    let roomIcon = this.props.room.get('iconUrl', null);
    if (!roomIcon) {
      roomIcon = '/public/images/blank.png';
    }

    const roomNameColor = this.props.currentRoom.getIn(['styles', 'sidebarTextColor']);

    const key = this.props.room.get('name');
    const active = this.props.active ? ' active' : '';
    const roomNameStyles = { marginLeft: '2px', color: roomNameColor };

    return (
      <li key={key} className={`roomlistentry ${active}`} onClick={this.onElementClicked}>
        <a className="roomselect" data-roomname={this.props.room.get('name')} style={this.getStyles()}
           onMouseLeave={this.onMouseLeave} onMouseEnter={this.onMouseEnter}
        >
          {this.renderUnreadCount(this.props)}
          {this.renderPrivateIconIfNecessary(this.props)}
          <img className="roomIconSmall" src={roomIcon}/>
          <span className={`roomname ${active}`} style={roomNameStyles}>
            #{this.props.room.get('displayName')}
          </span>
        </a>
      </li>
    );
  }
}

SidebarRoomListElm.defaultProps = {
  room: Immutable.Map(),
  active: false,
  unreadCount: 0
};


function mapStateToProps(state, ownProps) {
  const currentRoom = state.getIn(['rooms', state.get('currentRoom')]);
  const lastReadTime = state.getIn(['lastSeenTimes', ownProps.room.get('name')]);
  const unreadCount = state.getIn(['roomMessages', ownProps.room.get('name')]).reduce((total, messageId) => {
    const messageTime = state.getIn(['messages', messageId, 'createDateLongUTC']);
    return messageTime && messageTime - lastReadTime > 0 ? total + 1 : total;
  }, 0);

  return {
    currentRoom,
    unreadCount
  };
}

export default connect(mapStateToProps)(SidebarRoomListElm);
