import React, { Component } from 'react';


export default class ChatLoginInput extends Component {

  render() {
    const buttonBackground = this.props.room.getIn(['styles', 'signinButtonColor']);
    const buttonText = this.props.room.getIn(['styles', 'signinButtonTextColor']);
    const buttonStyles = {
      color: buttonText,
      backgroundColor: buttonBackground,
      border: 'none'
    };

    return (
      <a href={`/application/startauthforguest?roomName=${this.props.roomName}`}>
        <button type="submit" className="btn btn-lg btn-info btn-block" style={buttonStyles}>
          <i className="fa fa-reddit btn-addon pull-left" />&nbsp;&nbsp;Sign in with Reddit to chat
        </button>
      </a>
    );
  }
}

ChatLoginInput.defaultProps = {
  roomName: null
};
