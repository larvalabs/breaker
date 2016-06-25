import React, { Component } from 'react';
import { connect } from 'react-redux';
import ReactDOM from 'react-dom';
import Immutable from 'immutable';

import Config from '../config';

import SidebarRoomRoom from './SidebarRoom';
import { SidebarActiveRoom } from './SidebarActiveRoom';

import { scrollToRoomNameReset } from '../redux/actions/scroll-actions';
import { handleMoreActiveRooms, resetActiveRoomsList } from '../redux/actions/active-rooms-actions';

import { getSidebarOpen, getScrollToRoomName, getMoreActiveRoomsLoading, isActiveRoomsComplete } from '../redux/selectors/ui-selectors';
import { getAllRooms, getCurrentRoom, getCurrentRoomStyles } from '../redux/selectors/rooms-selectors';
import { getAllActiveRooms } from '../redux/selectors/active-rooms-selector';


class Sidebar extends Component {
  componentDidUpdate() {
    if (this.props.scrollToRoomName) {
      const roomList = ReactDOM.findDOMNode(this.refs.roomList);
      const scrollTo = ReactDOM.findDOMNode(this.refs[this.props.scrollToRoomName]);
      roomList.scrollTop = scrollTo.offsetTop;
      this.props.resetScrollToRoomName();
    }
  }

  getOrderedActiveRooms() {
    return this.props.activeRoomList.toArray().sort((a, b) => a.get('rank') - b.get('rank'));
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
              <SidebarRoomRoom key={room.get('name')}
                               ref={room.get('name')}
                               room={room}
                               active={room.get('name') === this.props.roomName}
                               styles={this.props.styles}
              />
            );
          })
        }
      </ul>
    );
  }

  renderLoadingIndicator() {
    const indicatorStyles = { fontSize: '0.5em', margin: '5px 60px' };
    return (
      <div style={indicatorStyles}>
        <i className="fa fa-circle-o-notch fa-spin fa-3x fa-fw"></i>
        <span className="sr-only">Loading...</span>
      </div>
    );
  }

  renderMoreActiveRoomsButton() {
    const { activeRoomsLoading } = this.props;
    const moreBtnStyles = { paddingLeft: '40px', margin: 0 };

    return (
      <li className="hidden-folded m-t m-b-sm text-muted" style={moreBtnStyles} onClick={this.props.moreActiveRooms}>
        {(activeRoomsLoading) ? this.renderLoadingIndicator() : <a><b>+ more</b></a>}
      </li>
    );
  }

  renderCollapseButton() {
    const styles = {
      float: 'right',
      fontSize: '1.1em'
    };

    return (
      <span style={styles}>
        <i className="fa fa-compress hoverable" onClick={ this.props.resetActiveRooms }></i>
      </span>
    );
  }

  renderActiveRooms() {
    const sidebarColor = this.props.room.getIn(['styles', 'sidebarTextColor']);
    const activeRoomsStyles = { color: sidebarColor };
    const { isActiveRoomsCompleted } = this.props;

    return (
      <ul id="roomlist" className="nav" style={{ marginBottom: '10px' }}>
        <li key="active-rooms" className="hidden-folded padder m-t m-b-sm text-muted text-xs">
          <span style={activeRoomsStyles}>Active Rooms</span>
          {(this.props.activeRoomList.toArray().length > 5) ? this.renderCollapseButton() : null}
        </li>
        {
          this.getOrderedActiveRooms()
              .filter((room) => {return !this.props.roomList.has(room.get('name'))})
              .map((room) => {
            return (
              <SidebarActiveRoom key={room.get('name')}
                                 room={room}
                                 styles={this.props.styles}
              />
            );
          })
        }
        {(this.props.activeRoomList.toArray().length > 4 && !isActiveRoomsCompleted) ? this.renderMoreActiveRoomsButton() : null}

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
              { this.renderYourRooms() }
              { (this.props.activeRoomList.toArray().length > 0) ? this.renderActiveRooms() : null }
            </nav>
          </div>
        </div>
      </aside>
    );
  }
}

Sidebar.defaultProps = {
  open: false,
  styles: Immutable.Map(),
  room: Immutable.Map(),
  roomName: null,
  roomList: Immutable.Map(),
  activeRoomList: Immutable.Map(),
  activeRoomsLoading: false,
  scrollToRoomName: null,
  resetScrollToRoomName: () => {
  },
  moreActiveRooms: () => {
  }
};

function mapStateToProps(state) {
  return {
    open: getSidebarOpen(state),
    styles: getCurrentRoomStyles(state),
    room: getCurrentRoom(state),
    roomName: state.get('currentRoom'),
    roomList: getAllRooms(state),
    activeRoomList: getAllActiveRooms(state),
    activeRoomsLoading: getMoreActiveRoomsLoading(state),
    isActiveRoomsCompleted: isActiveRoomsComplete(state),
    scrollToRoomName: getScrollToRoomName(state)
  };
}

function mapDispatchToProps(dispatch) {
  return {
    resetScrollToRoomName() {
      dispatch(scrollToRoomNameReset());
    },
    moreActiveRooms() {
      dispatch(handleMoreActiveRooms());
    },
    resetActiveRooms() {
      dispatch(resetActiveRoomsList());
    }
  };
}

export default connect(mapStateToProps, mapDispatchToProps)(Sidebar);
