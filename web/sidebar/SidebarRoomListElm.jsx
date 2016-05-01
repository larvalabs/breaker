import React, {Component} from 'react'
import { connect } from 'react-redux';
import Immutable from 'immutable'

import * as chatActions from '../redux/actions/chat-actions'

class SidebarRoomListElm extends Component {
  constructor(props){
    super(props);
    this.onElementClicked = this.onElementClicked.bind(this);
  }
  onElementClicked(){
    this.props.dispatch(chatActions.handleChangeRoom(this.props.room.get('name')));
  }
  renderUnreadCount(props){
    let backgroundColor = this.props.currentRoom.getIn(['styles', 'sidebarUnreadColor']);
    let textColor = this.props.currentRoom.getIn(['styles', 'sidebarUnreadTextColor']);

    if(props.unreadCount > 0) {
      return <b className="unreadcount label bg-info pull-right"
                style={{backgroundColor: backgroundColor, color: textColor}}>{this.props.unreadCount}</b>
    }
    return null;
  }
  render() {
    let roomIcon = this.props.room.get('iconUrl', null);
    if (!roomIcon) {
      roomIcon = '/public/images/blank.png';
    }

    let roomNameColor = this.props.currentRoom.getIn(['styles', 'sidebarTextColor']);

    let key = this.props.room.get('name');
    let active = this.props.active ? " active" : "";
    return <li key={key} className={`roomlistentry ${active}`} onClick={this.onElementClicked}>
      <a className="roomselect" data-roomname={this.props.room.get('name')}>
        {this.renderUnreadCount(this.props)}
        <img className="roomIconSmall" src={roomIcon}/>
        <span className={`roomname ${active}`}
              style={{marginLeft: "2px", color: roomNameColor}}>#{this.props.room.get('name')}</span>
      </a>
    </li>
  }
}

SidebarRoomListElm.defaultProps = {
  room: Immutable.Map(),
  active: false,
  unreadCount: 0
};


function mapStateToProps(state) {
  return {
    currentRoom: state.getIn(['rooms', state.getIn(['initial', 'roomName'])])
  }
}

export default connect(mapStateToProps)(SidebarRoomListElm)
