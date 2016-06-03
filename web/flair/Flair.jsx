import React, { Component } from 'react';
import { connect } from 'react-redux';

import Config from '../config';

import { getCurrentRoom } from '../redux/selectors/rooms-selectors';


class Flair extends Component {
  hasText(flairSettings) {
    return flairSettings.get('flairText') && flairSettings.get('flairText') !== 'null';
  }

  hasCssClass(flairSettings) {
    return flairSettings.get('flairCss') && flairSettings.get('flairCss') !== 'null';
  }

  hasFlair(flairSettings) {
    return this.hasCssClass(flairSettings) || this.hasText(flairSettings);
  }

  renderFlair() {
    const flairSettings = this.props.user.getIn(['flair', this.props.room.get('name')]);
    if (!flairSettings) {
      return null;
    }

    if (!this.hasFlair(flairSettings)) {
      return null;
    }

    const classes = `flair flair-${flairSettings.get('flairCss')}`;

    if (!this.hasText(flairSettings) || this.props.classOnly) {
      return <span className={classes} title={flairSettings.get('flairText')}></span>;
    }

    return <span className={classes} title={flairSettings.get('flairText')}>{flairSettings.get('flairText')}</span>;
  }

  render() {
    const flairScaleClass = Config.settings.flairScaleForRoom(this.props.room);
    return (
      <div className={`flair-container ${flairScaleClass}`}>
        {this.renderFlair()}
      </div>
    );
  }
}


function mapStateToProps(state) {
  return {
    room: getCurrentRoom(state)
  };
}

export default connect(mapStateToProps)(Flair);
