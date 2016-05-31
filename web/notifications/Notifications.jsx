import React, { Component } from 'react';
import { connect } from 'react-redux';
import Notification from 'react-web-notification';

import Config from '../config';

import * as notifyActions from '../redux/actions/notification-actions';


class Notifications extends Component {
  render() {
    if (Config.guest) {
      return null;
    }

    const { notification } = this.props;
    let options = null;
    if (notification.get('options')) {
      options = notification.get('options').toJS();
    }

    return (
        <div>
          <Notification
              ignore={notification.get('ignore')}
              notSupported={this.props.handleNotSupported}
              onPermissionGranted={this.props.handlePermissionGranted}
              onPermissionDenied={this.props.handlePermissionDenied}
              onShow={this.props.handleNotificationOnShow}
              onClick={this.props.handleNotificationOnClick}
              onClose={this.props.handleNotificationOnClose}
              onError={this.props.handleNotificationOnError}
              timeout={Config.settings.notification_timeout}
              title={notification.get('title')}
              options={options}
          />
        </div>
    )
  }
}

function mapStateToProps(state) {
  return {
    notification: state.get('notification')
  };
}

function mapDispatchToProps(dispatch) {
  return {
    handleNotSupported(event) {
      return dispatch(notifyActions.handleNotSupported(event));
    },
    handlePermissionGranted(event) {
      return dispatch(notifyActions.handlePermissionGranted(event));
    },
    handlePermissionDenied(event) {
      return dispatch(notifyActions.handlePermissionDenied(event));
    },
    handleNotificationOnShow(event) {
      return dispatch(notifyActions.handleNotificationOnShow(event));
    },
    handleNotificationOnClick(event) {
      return dispatch(notifyActions.handleNotificationOnClick(event));
    },
    handleNotificationOnClose(event) {
      return dispatch(notifyActions.handleNotificationOnClose(event));
    },
    handleNotificationOnError(event) {
      return dispatch(notifyActions.handleNotificationOnError(event));
    }
  };
}

export default connect(mapStateToProps, mapDispatchToProps)(Notifications);
