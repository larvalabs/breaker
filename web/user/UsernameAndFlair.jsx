import React, { Component } from 'react';
import Immutable from 'immutable';

import Flair from '../flair/Flair';


export default class UsernameAndFlair extends Component {
  renderFlairIfNecessary() {
    const { user, noFlair, classOnly } = this.props;
    if (noFlair) {
      return null;
    }

    return (
        <Flair user={user} classOnly={classOnly}/>
    );
  }

  render() {
    const { user, messageUsername } = this.props;

    const modClass = user.get('modForRoom') ? 'text-md text-primary-dker' : 'text-md text-dark-dker';
    let username = user.get('username');
    if (!username) {
      username = messageUsername;
    }

    if (username === 'breakerbotsystem') {
      return null;
    }

    return (
      <div className="message-container">
        <div className="username-container">
          <a className={modClass} href={`https://reddit.com/u/${user.get('username')}`} target="_blank">
            {username}</a>
        </div>
        {this.renderFlairIfNecessary()}
      </div>
    );
  }
}

UsernameAndFlair.defaultProps = {
  user: Immutable.Map(),
  classOnly: false,
  noFlair: false,
  messageUsername: null
};
