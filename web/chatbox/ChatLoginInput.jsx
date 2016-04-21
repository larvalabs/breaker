import React, {Component} from 'react';

export default class ChatLoginInput extends Component {

  render() {
      return <a href={`/application/startauthforguest?roomName=${this.props.roomName}`}>
        <button type="submit" className="btn btn-lg btn-info btn-block">
          <i className="fa fa-reddit btn-addon pull-left"></i>&nbsp;&nbsp;Sign in with Reddit to chat
        </button>
      </a>
  }
}

ChatLoginInput.defaultProps = {
  roomName: null
};
