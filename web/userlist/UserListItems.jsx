import React, { Component } from 'react';
import Immutable from 'immutable';

import UserListItem from './UserListItem.jsx';


export default class UserListItems extends Component {
  renderMessage() {
    const messageStyle = { textAlign: 'center' };

    return (
      <div style={messageStyle}>
        <i>{this.props.children}</i>
      </div>
    );
  }

  renderItems() {
    const { items, roomName } = this.props;

    return (
        <ul className="list-group no-bg no-borders pull-in m-b-sm">
          {items.map((user) => <UserListItem key={user.get('username')} user={user} roomName={roomName}/>)}
        </ul>
    );
  }

  renderItemsOrMessage() {
    const { items } = this.props;

    if (items.size < 1) {
      return this.renderMessage();
    }
    return this.renderItems();
  }

  render() {
    const { title } = this.props;

    return (
      <div className="wrapper-md m-b-n-md">
        <div className="m-b-sm text-md">{title}</div>
        {this.renderItemsOrMessage()}
      </div>
    );
  }
}

UserListItems.defaultProps = {
  items: Immutable.List(),
  roomName: '',
  title: ''
};
