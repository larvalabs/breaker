import React, {Component} from 'react';

export default class ChatLoginInput extends Component {

  render() {
    let buttonBackground = this.props.room.getIn(['styles', 'signinButtonColor']);
    let buttonText = this.props.room.getIn(['styles', 'signinButtonTextColor']);

    return <a href={`/application/startauthforguest?roomName=${this.props.roomName}`}>
        <button type="submit" className="btn btn-lg btn-info btn-block"
                style={{color: buttonText, backgroundColor: buttonBackground, border: "none"}}>
          <i className="fa fa-reddit btn-addon pull-left"></i>&nbsp;&nbsp;Sign in with Reddit to chat
        </button>
      </a>
  }
}

ChatLoginInput.defaultProps = {
  roomName: null
};
