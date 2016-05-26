import React, { Component } from 'react';
import { connect } from 'react-redux';
import Immutable from 'immutable';

import { toggleSidebar } from '../redux/actions/menu-actions';


class MobileMenuButton extends Component {
  render() {
    const classes = 'pull-right visible-xs';
    const active = this.props.sidebar_open ? ' active' : '';
    const iconColor = this.props.room.getIn(['styles', 'sidebarTextColor'], '#EAEBED');
    const iconBGColor = this.props.room.getIn(['styles', 'sidebarBackgroundColor'], '#3a3f51');
    const iconStyles = { color: iconColor, background: iconBGColor };

    return (
      <button className={classes + active} ui-toggle-className="off-screen"
              target=".app-aside" ui-scroll="app" onClick={this.props.toggleSidebar}
      >
        <i className="glyphicon glyphicon-align-justify" style={iconStyles}/>
      </button>
    );
  }
}

MobileMenuButton.defaultProps = {
  room: Immutable.Map(),
  sidebar_open: false
};

function mapDispatchToProps(dispatch) {
  return {
    toggleSidebar() {
      return dispatch(toggleSidebar());
    }
  };
}

export default connect(null, mapDispatchToProps)(MobileMenuButton);
