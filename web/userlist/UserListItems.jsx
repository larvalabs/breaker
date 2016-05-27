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

    const wrapperStyles = {
      padding: '.5em 1.2em'
    };

    const titleStyles = {
      borderBottom: '1px solid #D6D7D8',
      paddingBottom: '.25em',
      marginBottom: '0.5em',
    };

    const listStyles = {
      padding: '0 0.5em'
    };

    return (
      <div className="wrapper-md m-b-n-md" style={wrapperStyles}>
        <div className="m-b-sm text-md" style={titleStyles}>{title}</div>
        <div style={listStyles}>
          {this.renderItemsOrMessage()}
        </div>
      </div>
    );
  }
}

UserListItems.defaultProps = {
  items: Immutable.List(),
  roomName: '',
  title: ''
};
