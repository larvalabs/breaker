import React, {Component} from 'react'
import Immutable from 'immutable'
import { connect } from "react-redux";
import { handleScrollToNextUnread } from '../redux/actions/scroll-actions'


export default class HeaderUnreadCount extends Component {
  render(){
    let backgroundColor = this.props.room.getIn(['styles', 'sidebarUnreadColor']);
    let textColor = this.props.room.getIn(['styles', 'sidebarUnreadTextColor']);
    if(this.props.unreadCount < 1) {
      return null;
    }

    return <div className="unread-count-total pull-right">
      <b className="unreadcount label bg-info pull-right"
         title="Jump to next unread room"
         style={{backgroundColor: backgroundColor, color: textColor, cursor: "pointer"}}
         onClick={this.props.scrollToUnreadRoom}>{this.props.unreadCount}</b>
    </div>
  }
}

HeaderUnreadCount.defaultProps = {
  room: Immutable.Map(),
  unreadCount: 0
};

function mapDispatchToProps(dispatch){
  return {
    scrollToUnreadRoom(){
      dispatch(handleScrollToNextUnread())
    }
  }
}

export default connect(null, mapDispatchToProps)(HeaderUnreadCount)
