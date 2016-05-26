import React, { Component } from 'react';
import ReactDOM from 'react-dom';
import Immutable from 'immutable';

import Config from '../config';

import SidebarRoomListElm from './SidebarRoomListElm';


export default class Sidebar extends Component {
  componentDidUpdate() {
    if (this.props.scrollToRoomName) {
      const roomList = ReactDOM.findDOMNode(this.refs.roomList);
      const scrollTo = ReactDOM.findDOMNode(this.refs[this.props.scrollToRoomName]);
      roomList.scrollTop = scrollTo.offsetTop;
      this.props.scrollToRoomNameReset();
    }
  }

  renderYourRooms() {
    const sidebarColor = this.props.room.getIn(['styles', 'sidebarTextColor']);
    const yourRoomsStyles = { color: sidebarColor };
    return (
      <ul id="roomlist" className="nav">
        <li key="your-rooms" className="hidden-folded padder m-t m-b-sm text-muted text-xs">
          <span style={yourRoomsStyles}>Your Rooms</span>
        </li>
        {
          this.props.roomList.toArray().map((room) => {
            return (
                <SidebarRoomListElm key={room.get('name')}
                                    ref={room.get('name')}
                                    room={room}
                                    active={room.get('name') === this.props.roomName}
                />
            );
          })
        }
      </ul>
    );
  }

  render() {
    const styles = Config.styles.getSidebarColorForRoom(this.props.room);

    let classes = 'app-aside hidden-xs bg-dark';
    if (this.props.open) {
      classes += ' off-screen';
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
    );
  }
}

Sidebar.defaultProps = {
  roomList: Immutable.Map(),
  roomName: null
};
