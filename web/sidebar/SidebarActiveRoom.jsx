import React from 'react';

import SidebarRoomName from './SidebarRoomName';
import SidebarRoomAvatar from './SidebarRoomAvatar';
import SidebarRoomSelect from './SidebarRoomSelect';

export const SidebarActiveRoom = (props) => {
  const { styles, room } = props;

  const renderActiveUsers = () => {
    const activeUsersStyles = { backgroundColor: '#b4b6bd', color: '#3a3f51' };

    if (room.get('activeUsers') > 0) {
      return (
        <div className="unread-count-room pull-right">
          <b className="unreadcount label bg-info pull-right" style={activeUsersStyles}>{room.get('activeUsers')}</b>
        </div>);
    } else {
      return (
          <div className="unread-count-room pull-right">

          </div>);
    }
  };

  const joinRoom = () => {
    const url = `${window.location.origin}/r/${room.get('name')}`;
    window.location.href = url;
  };

  return (
    <li className="roomlistentry" onClick={joinRoom} title="Active Users">
      <SidebarRoomSelect styles={styles} active={false}>
        <SidebarRoomAvatar room={room}/>
        <SidebarRoomName room={room} styles={styles}/>
        {renderActiveUsers()}
      </SidebarRoomSelect>
    </li>
  );
};
