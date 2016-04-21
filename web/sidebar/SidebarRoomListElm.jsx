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
    this.props.dispatch(chatActions.changeRoom(this.props.room.get('name')));
  }
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

    let key = this.props.room.get('name');
    let className = "roomlistentry" + (this.props.active ? " active" : "");
    return <li key={key} className={className} onClick={this.onElementClicked}>
      <a className="roomselect" data-roomname={this.props.room.get('name')}>
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


function mapStateToProps(state) {
  return {
  }
}

export default connect(mapStateToProps)(SidebarRoomListElm)
