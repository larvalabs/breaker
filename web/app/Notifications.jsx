import React, { Component, PropTypes } from 'react'
import { connect } from 'react-redux'
import Notification from 'react-web-notification'
import Config from '../config'
import * as notifyActions from '../redux/actions/notification-actions'

class Notifications extends Component {
  playSound(filename){
    // document.getElementById('sound').play();
  }
  
  render() {
    const { notification } = this.props;
    let options = null;
    if(notification.get('options')){
      options = notification.get('options').toJS()
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
  }
}

function mapDispatchToProps(dispatch){
  return {
    handleNotSupported(){
      return dispatch(notifyActions.handleNotSupported())
    },
    handlePermissionGranted(){
      return dispatch(notifyActions.handlePermissionGranted())
    },
    handlePermissionDenied(){
      return dispatch(notifyActions.handlePermissionDenied())
    },
    handleNotificationOnShow(){
      return dispatch(notifyActions.handleNotificationOnShow())
    },
    handleNotificationOnClick(){
      return dispatch(notifyActions.handleNotificationOnClick())
    },
    handleNotificationOnClose(){
      return dispatch(notifyActions.handleNotificationOnClose())
    },
    handleNotificationOnError(){
      return dispatch(notifyActions.handleNotificationOnError())
    }
  }
}

export default connect(mapStateToProps, mapDispatchToProps)(Notifications)
