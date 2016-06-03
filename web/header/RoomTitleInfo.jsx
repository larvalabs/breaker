import React, { Component } from 'react';
import Immutable from 'immutable';

import Config from '../config';

import RoomTitleActions from './RoomTitleActions';


export default class RoomIcon extends Component {
  render() {
    const { room } = this.props;

    if (!room.get('name')) {
      return null;
    }

    const modMessageStyles = { marginLeft: '10px' };

    return (
      <ul className="nav navbar-nav hidden-sm" >
        <li className="m-t-xs m-b-xxs middle" >
          <span id="room-title" className="h4 m-n font-thin h4 text-black">
            <a href={`https://reddit.com/r/${room.get('name')}`} target="_blank">#{room.get('displayName')}</a></span>
          <RoomTitleActions room={room} userIsMod={this.props.userIsMod}/>
          <br/>
          <small style={modMessageStyles} id="room-modmessage" className="text-muted">
            {room.get('banner') ? room.get('banner') : Config.settings.default_banner}
          </small>
        </li>
      </ul>
    );
  }
}

RoomIcon.defaultProps = {
  room: Immutable.Map(),
  userIsMod: false
};
