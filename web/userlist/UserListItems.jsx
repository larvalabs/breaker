import React, { Component } from 'react';
import Immutable from 'immutable';

import UserListItem from './UserListItem.jsx';


export default class UserListItems extends Component {

  constructor(props) {
    super(props);

    const filteredList = this.getFilteredList(props);

    this.state = {
      filteredList: filteredList || [],
      filteredUserCount: filteredList.size || 0
    };
  }

  componentWillReceiveProps(nextProps) {
    const filteredList = this.getFilteredList(nextProps);
    this.setState({ filteredList, filteredUserCount: filteredList.size });
  }

  getOrderedList(props) {
    const { items, orderBy, order } = props;
    let orderedList = items.toArray();

    if (orderedList.length > 0 && orderedList[0].toJS().hasOwnProperty(orderBy)) {
      orderedList = items.sort((item1, item2) => {
        let prop1 = item1.get(orderBy);
        let prop2 = item2.get(orderBy);

        prop1 = (this.isString(prop1)) ? prop1.toLowerCase() : prop1;
        prop2 = (this.isString(prop2)) ? prop2.toLowerCase() : prop2;

        if (order.toLowerCase().trim() === 'desc') {
          return (prop1 < prop2) ? 1 : -1;
        }

        return (prop1 < prop2) ? -1 : 1;
      });
    }

    return orderedList;
  }

  getFilteredList(props) {
    const { filterBy } = props;

    return this.getOrderedList(props)
      .filter((user) => user.get('username').toLowerCase().indexOf(filterBy) > -1 && user.get('username') !== 'guest');
  }

  isString(value) {
    return typeof value === 'string' || value instanceof String;
  }

  renderMessage() {
    const messageStyle = { textAlign: 'center', marginBottom: '2em' };

    return (
      <div style={messageStyle}>
        <i>{this.props.children}</i>
      </div>
    );
  }

  renderItems() {
    const { roomName, filterBy, maximum, items } = this.props;
    const max = (maximum === -1 || filterBy.trim().length > 0) ? items.length : maximum;

    return (
      <ul className="list-group no-bg no-borders pull-in m-b-sm">
        {
          this.state.filteredList
            .slice(0, max)
            .map((user) => <UserListItem key={user.get('username')} user={user} roomName={roomName}/>)
        }
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
    const { title, showMore, items, maximum } = this.props;

    const styles = {
      userCount: {
        float: 'right',
        backgroundColor: '#98a6ad',
        fontWeight: 'normal'
      },
      wrapperStyles: {
        padding: '.5em 1.2em'
      },
      titleStyles: {
        borderBottom: '1px solid #D6D7D8',
        paddingBottom: '.25em',
        marginBottom: '0.5em',
      },
      listStyles: {
        padding: '0 0.5em'
      },
      moreBtn: {
        fontWeight: 'bold',
        margin: '0 10px'
      }
    };

    return (
      <div className="wrapper-md m-b-n-md" style={styles.wrapperStyles}>
        <div className="m-b-sm text-md" style={styles.titleStyles}>
          {title}
          <span className="badge" style={styles.userCount}>{this.state.filteredUserCount || 0}</span>
        </div>
        <div style={styles.listStyles}>
          {this.renderItemsOrMessage()}
        </div>
        {(maximum !== -1 && maximum < items.toArray().length) ? <a onClick={ showMore } style={styles.moreBtn}>+ more</a> : null}
      </div>
    );
  }
}

UserListItems.defaultProps = {
  items: Immutable.List(),
  roomName: '',
  title: '',
  filterBy: '',
  orderBy: 'username',
  order: 'asc', // or desc
  maximum: -1, // -1 = no limit
  showMore: () => {}
};
