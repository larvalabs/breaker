import React, {Component} from 'react'


export default class SidebarRoomListElm extends Component {
  isRoomActiveRoom(){
    //TODO: check for this
    return false;
  }
  render() {
    return <li className={"roomlistentry" + (this.isRoomActiveRoom() ? " active" : "")}>
      <a className="roomselect" data-roomname={this.props.room.name}>
        <img className="roomIconSmall"
             src={this.props.room.iconUrl ? this.props.room.iconUrl : '/public/images/blank.png'}/>
        <b className="unreadcount label bg-info pull-right" />
        <span className="roomname">#{this.props.room.name}</span>
      </a>
    </li>
  }
}

SidebarRoomListElm.defaultProps = {
  room: {}
};
