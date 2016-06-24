import React, { Component } from 'react';
import Immutable from 'immutable';

import Config from '../config';


export default class HeaderUserMenu extends Component {
  renderUsername() {
    var avatarStyleOverrides = {
      objectFit: 'cover',
      height: '40px'
    };
    return (
      <div>
        <span className="thumb-sm avatar pull-right m-t-n-sm m-b-n-sm m-l-sm">
          <img src={this.props.user.get('profileImageUrl')} alt="..." style={avatarStyleOverrides} />
          <i className="on md b-white bottom" />
        </span>
        <span className="hidden-sm hidden-md">{this.props.user.get('username')}</span>
        <b className="caret" />
      </div>
    );
  }

  renderProfileMenu() {
    return (
      <ul className="nav navbar-nav navbar-right">
        <li className="dropdown v-middle">
          <a href="#" data-toggle="dropdown" className="dropdown-toggle clear" data-toggle="dropdown">
            {this.renderUsername()}
          </a>
          <ul className="dropdown-menu w">
            <li>
              <a ui-sref="access.signin" href="/usermanage/prefs">Preferences</a>
            </li>
            <li>
              <a ui-sref="access.signin" href="/logout">Logout</a>
            </li>
          </ul>
        </li>
      </ul>
    );
  }

  renderLogin() {
    const signInStyles = {
      textAlign: 'center',
      paddingRight: '1em',
      fontWeight: '600'
    };

    let signInUrl = `/application/startauthforguest?roomName=${this.props.room.get('name')}`;
    if (window.innerWidth < 850) {
      signInUrl += '&compact=true';
    }
    return (
      <ul className="nav navbar-nav navbar-right">
        <li style={signInStyles}>
          <a href={signInUrl}>
            Sign in
          </a>
        </li>
      </ul>
    );
  }

  render() {
    if (Config.guest) {
      return this.renderLogin();
    }

    return this.renderProfileMenu();
  }
}

HeaderUserMenu.defaultProps = {
  room: Immutable.Map(),
  user: Immutable.Map()
};
