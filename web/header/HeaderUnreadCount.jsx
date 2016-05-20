import React, {Component} from 'react'
import Immutable from 'immutable'


export default class HeaderUnreadCount extends Component {
  render(){
    let backgroundColor = this.props.room.getIn(['styles', 'sidebarUnreadColor']);
    let textColor = this.props.room.getIn(['styles', 'sidebarUnreadTextColor']);
    if(this.props.unreadCount < 1) {
      return null;
    }

    return <div className="unread-count-total pull-right">
      <b className="unreadcount label bg-info pull-right"
         style={{backgroundColor: backgroundColor, color: textColor}}>{this.props.unreadCount}</b>
    </div>
  }
}

HeaderUnreadCount.defaultProps = {
  room: Immutable.Map(),
  unreadCount: 0
};
