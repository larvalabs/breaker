import React, { Component } from 'react';
import { connect } from 'react-redux';
import Immutable from 'immutable';

import UserListItems from './UserListItems';
import UserListScrollable from './UserListScrollable';

import {
  getModUsersForCurrentRoom,
  getOnlineUsersForCurrentRoom,
  getOfflineUsersForCurrentRoom
} from '../redux/selectors/users-selectors';


class UserListBox extends Component {

  constructor() {
    super();

    this.handleChange = this.handleChange.bind(this);
    this.handleMoreOffline = this.handleMoreOffline.bind(this);

    this.state = {
      searchTerm: '',
      offlineMaximum: 1
    };
  }

  handleChange(event) {
    this.setState({ searchTerm: event.target.value });
  }

  handleMoreOffline() {
    this.setState({ offlineMaximum: this.state.offlineMaximum + 10 });
  }

  render() {
    const { mods, online, offline, roomName } = this.props;
    return (
      <UserListScrollable>
        <div className="input-group" style={{ margin: '15px  ' }}>
          <div className="input-group-addon"><span className="fa fa-search"></span></div>
          <input type="text" className="form-control input-sm" placeholder="search..."
                 value={this.state.searchTerm}
                 onChange={this.handleChange}
          />
        </div>
        <UserListItems items={mods} roomName={roomName} title="Mods" filterBy={this.state.searchTerm}>No mods online</UserListItems>
        <UserListItems items={online} roomName={roomName} title="Here Now" filterBy={this.state.searchTerm}>No users online</UserListItems>
        <UserListItems items={offline} roomName={roomName} title="Offline"
                       filterBy={this.state.searchTerm}
                       orderBy="lastSeen"
                       order="desc"
                       maximum={this.state.offlineMaximum}
                       showMore={this.handleMoreOffline }
        >No users offline</UserListItems>
      </UserListScrollable>
    );
  }
}

UserListBox.defaultProps = {
  online: Immutable.Map(),
  offline: Immutable.Map(),
  mods: Immutable.Map(),
  roomName: ''
};

function mapStateToProps(state) {
  return {
    roomName: state.get('currentRoom'),
    online: getOnlineUsersForCurrentRoom(state),
    mods: getModUsersForCurrentRoom(state),
    offline: getOfflineUsersForCurrentRoom(state)
  };
}

export default connect(mapStateToProps)(UserListBox);
