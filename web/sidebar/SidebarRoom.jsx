import React, { Component } from 'react';
import { connect } from 'react-redux';
import Immutable from 'immutable';

import * as chatActions from '../redux/actions/chat-actions';

import SidebarRoomUnreadCount from './SidebarRoomUnreadCount';
import SidebarRoomPrivateIcon from './SidebarRoomPrivateIcon';
import SidebarRoomAvatar from './SidebarRoomAvatar';
import SidebarRoomSelect from './SidebarRoomSelect';
import SidebarRoomName from './SidebarRoomName';


class SidebarRoomListElm extends Component {
  constructor(props) {
    super(props);
    this.onElementClicked = this.onElementClicked.bind(this);
  }

  onElementClicked(event) {
    const { room, changeRoom } = this.props;

    if (event.target.className.indexOf('close') < 0) {
      changeRoom(room.get('name'));
    }
  }

  render() {
    const { styles, room, active } = this.props;
    const liStyles = `roomlistentry ${active ? 'active' : ''}`;

    return (
      <li className={liStyles} onClick={this.onElementClicked}>
        <SidebarRoomSelect styles={styles} active={active}>
          <SidebarRoomUnreadCount room={room} styles={styles}/>
          <SidebarRoomPrivateIcon room={room} styles={styles}/>
          <SidebarRoomAvatar room={room}/>
          <SidebarRoomName room={room} styles={styles}/>
        </SidebarRoomSelect>
      </li>
    );
  }
}

SidebarRoomListElm.defaultProps = {
  styles: Immutable.Map(),
  room: Immutable.Map(),
  active: false
};

function mapDispatchToProps(dispatch) {
  return {
    changeRoom(roomName) {
      dispatch(chatActions.handleChangeRoom(roomName));
    }
  };
}

export default connect(null, mapDispatchToProps)(SidebarRoomListElm);
