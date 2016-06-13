import React from 'react';

import SidebarRoomName from './SidebarRoomName';
import SidebarRoomAvatar from './SidebarRoomAvatar';
import SidebarRoomSelect from './SidebarRoomSelect';

export const SidebarActiveRoom = (props) => {
  const { styles, room } = props;

  const renderActiveUsers = () => {
    const activeUsersStyles = { backgroundColor: '#B73030', color: '#F8DCDC' };

    return (
      <div className="unread-count-room pull-right">
        <b className="unreadcount label bg-info pull-right" style={activeUsersStyles}>{room.get('activeUsers')}</b>
      </div>
    );
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
