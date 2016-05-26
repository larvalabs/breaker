import React, { Component } from 'react';
import { connect } from 'react-redux';
import Immutable from 'immutable';

import { toggleSettings } from '../redux/actions/menu-actions';


class MobileSettingsButton extends Component {
  render() {
    let classes = 'pull-right visible-xs';
    if (this.props.settings_open) {
      classes += ' active';
    }

    const iconColor = this.props.room.getIn(['styles', 'sidebarTextColor'], '#EAEBED');
    const iconBGColor = this.props.room.getIn(['styles', 'sidebarBackgroundColor'], '#3a3f51');
    const buttonStyles = { backgroundColor: iconBGColor };
    const iconStyles = { color: iconColor };

    return (
      <button className={classes}
              ui-toggle-className="show"
              target=".navbar-collapse"
              onClick={this.props.toggleSettings}
              style={buttonStyles}
      >
        <i className="glyphicon glyphicon-cog" style={iconStyles}/>
      </button>
    );
  }
}

MobileSettingsButton.defaultProps = {
  room: Immutable.Map(),
  settings_open: false
};

function mapDispatchToProps(dispatch) {
  return {
    toggleSettings() {
      return dispatch(toggleSettings());
    }
  };
}

export default connect(null, mapDispatchToProps)(MobileSettingsButton);
