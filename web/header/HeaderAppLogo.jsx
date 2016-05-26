import React, { Component } from 'react';
import Immutable from 'immutable';

import Config from '../config';


export default class HeaderAppLogo extends Component {
  render() {
    const styles = Config.styles.getSidebarColorForRoom(this.props.room);
    const sidebarColor = this.props.room.getIn(['styles', 'sidebarTextColor']);

    return (
      <a href="#" className="navbar-brand text-lt" style={styles}>
        <i className="fa fa-terminal" style={ { color: sidebarColor } }/>
        <span className="hidden-folded m-l-xs" style={ { marginLeft: '10px', color: sidebarColor } }>breaker</span>
      </a>
    );
  }
}

HeaderAppLogo.defaultProps = {
  room: Immutable.Map()
};
