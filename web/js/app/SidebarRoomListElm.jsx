import React, {Component} from 'react'


export default class SidebarRoomListElm extends Component {
  render() {
    return <li key={this.props.room.name} className={"roomlistentry" + (this.props.active ? " active" : "")}>
      <a href={`/r/${this.props.room.name}`}  className="roomselect" data-roomname={this.props.room.name}>
        <img className="roomIconSmall"
             src={this.props.room.iconUrl ? this.props.room.iconUrl : '/public/images/blank.png'}/>
        <b className="unreadcount label bg-info pull-right" />
        <span className="roomname">#{this.props.room.name}</span>
      </a>
    </li>
  }
}

SidebarRoomListElm.defaultProps = {
  room: {},
  active: false
};
