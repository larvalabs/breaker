import React, {Component} from 'react'
import Immutable from 'immutable'

export default class SidebarRoomListElm extends Component {
  renderUnreadCount(props){
    if(props.unreadCount > 0) {
      return <b className="unreadcount label bg-info pull-right">{this.props.unreadCount}</b>
    }
    return null;
  }
  render() {
    let roomIcon = this.props.room.get('iconUrl', null);
    if (!roomIcon) {
      roomIcon = '/public/images/blank.png';
    }

    return <li key={this.props.room.get('name')} className={"roomlistentry" + (this.props.active ? " active" : "")}>
      <a href={`/r/${this.props.room.get('name')}`}  className="roomselect" data-roomname={this.props.room.get('name')}>
        {this.renderUnreadCount(this.props)}
        <img className="roomIconSmall" src={roomIcon}/>
        <span className="roomname">#{this.props.room.get('name')}</span>
      </a>
    </li>
  }
}

SidebarRoomListElm.defaultProps = {
  room: Immutable.Map(),
  active: false,
  unreadCount: 0
};
