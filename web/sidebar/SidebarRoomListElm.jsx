import React, {Component} from 'react'
import Immutable from 'immutable'

export default class SidebarRoomListElm extends Component {
  render() {
    let roomIcon = this.props.room.get('iconUrl', null);
    if (!roomIcon) {
      roomIcon = '/public/images/blank.png';
    }

    return <li key={this.props.room.get('name')} className={"roomlistentry" + (this.props.active ? " active" : "")}>
      <a href={`/r/${this.props.room.get('name')}`}  className="roomselect" data-roomname={this.props.room.get('name')}>
        <img className="roomIconSmall" src={roomIcon}/>
        <b className="unreadcount label bg-info pull-right" />
        <span className="roomname">#{this.props.room.get('name')}</span>
      </a>
    </li>
  }
}

SidebarRoomListElm.defaultProps = {
  room: Immutable.Map(),
  active: false
};
