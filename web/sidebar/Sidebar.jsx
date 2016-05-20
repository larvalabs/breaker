import React from 'react';
import Config from '../config'
import Immutable from 'immutable'
import ReactDOM from 'react-dom'
import SidebarRoomListElm from './SidebarRoomListElm'

export default React.createClass({
  componentDidUpdate() {
    if(this.props.scrollToRoomName) {
      let roomList = ReactDOM.findDOMNode(this.refs.roomList);
      let scrollTo = ReactDOM.findDOMNode(this.refs[this.props.scrollToRoomName]);
      roomList.scrollTop = scrollTo.offsetTop;
      this.props.scrollToRoomNameReset();
    }
  },
  getDefaultProps: function(){
    return {
      roomList: Immutable.Map(),
      roomName: null,
    }
  },
  renderYourRooms: function(){

    let sidebarColor = this.props.room.getIn(['styles', 'sidebarTextColor']);

    return <ul id="roomlist" className="nav">
      <li key="your-rooms" className="hidden-folded padder m-t m-b-sm text-muted text-xs">
        <span style={{color: sidebarColor}}>Your Rooms</span>
      </li>
      {
        this.props.roomList.toArray().map((room) => {
            return <SidebarRoomListElm key={room.get('name')}
                                       ref={room.get('name')}
                                       room={room}
                                       active={room.get('name') == this.props.roomName}/>;
        })
      }
    </ul>
  },
  render: function () {
    let classes = "app-aside hidden-xs bg-dark";
    let styles = Config.styles.getSidebarColorForRoom(this.props.room);
    if(this.props.open){
      classes += " off-screen";
    }
    return (
      <aside id="aside" className={classes} style={styles}>
        <div className="aside-wrap">
          <div className="navi-wrap" ref="roomList">
            <nav ui-nav className="navi clearfix">
              {this.renderYourRooms()}
            </nav>
          </div>
        </div>
      </aside>
    )
  }
})
