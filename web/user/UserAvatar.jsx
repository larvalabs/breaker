import React, { Component } from 'react';
import Config from '../config';


export default class UserAvatar extends Component {
  render() {
    const { user } = this.props;

    if (user.get('username') === 'breakerbotsystem') {
      return null;
    }

    let userImage = user.get('profileImageUrl');
    if (!userImage) {
      userImage = '/public/img/user-anon.png';
    }

    let avatarStyleOverrides = {};
    if (userImage.indexOf('user-anon.png') > 0) {
      avatarStyleOverrides = { opacity: '0.2' };
    }
    avatarStyleOverrides.height = '40px';
    avatarStyleOverrides.objectFit = 'cover';
    // avatarStyleOverrides.backgroundImage = 'url(' + userImage + ')';
    // avatarStyleOverrides.backgroundPosition = 'center center';
    // avatarStyleOverrides.backgroundRepeat = 'no-repeat';

    const userLink = `https://reddit.com/u/${user.get('username')}`;
    const onlineClass = user.get('online') ? 'on' : 'off';

    return (
      <a href={userLink} className="pull-left thumb-sm avatar m-r hidden-xs" target="_blank">
        <img src={userImage} alt="..." className="img-circle" style={avatarStyleOverrides}/>
        <i className={`${onlineClass} b-white bottom`} />
      </a>
    );
  }
}
