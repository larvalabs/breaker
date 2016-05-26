import React, { Component } from 'react';
import { connect } from 'react-redux';
import Immutable from 'immutable';

import Config from '../config';
import StayDown from 'staydown';

import ChatThread from './ChatThread';
import ChatMessageInput from './ChatMessageInput';
import ChatLoginInput from './ChatLoginInput';

const $ = window.jQuery;

class ChatBox extends Component {
  constructor(props) {
    super(props);
    this.onMessageInput = this.onMessageInput.bind(this);
    this.renderMessageInput = this.renderMessageInput.bind(this);
  }
  componentDidMount() {
    const staydown = new StayDown({
      target: $('#thread_scrollparent')[0],
      interval: 500
    });

    const checkdown = function () {
      staydown.checkdown();
      window.setTimeout(checkdown, staydown.interval);
    };
    checkdown();

    this.staydown = staydown;
  }

  componentWillReceiveProps(nextProps) {
    if (this.props.roomName !== nextProps.roomName) {
      this.staydown.intend_down = true;
    }
  }

  onMessageInput() {
    this.staydown.intend_down = true;
  }

  renderMessage(props) {
    const body = props.message.get('body');
    const type = props.message.get('type');

    if (!body || !type) {
      return null;
    }

    return <div className={`message-box ${type}`}>{props.message.get('body')}</div>;
  }
  renderMessageInput() {
    if (Config.guest) {
      return <ChatLoginInput roomName={this.props.roomName} room={this.props.room}/>;
    }

    return <ChatMessageInput roomName={this.props.roomName} onMessageInput={this.onMessageInput}/>;
  }
  render() {
    return (
      <div id="centercol" className="col">
        <div id="threadparent" className="vbox">
          {this.renderMessage(this.props)}
          <ChatThread users={this.props.users} roomName={this.props.roomName}/>
          <div className="input-container padder padder-v b-t b-light text-center">
            {this.renderMessageInput()}
          </div>
        </div>
      </div>
    );
  }
}

ChatBox.defaultProps = {
  messages: []
};

function mapStateToProps(state) {
  const roomName = state.get('currentRoom');

  return {
    users: state.get('users'),
    roomName,
    message: state.get('bannerMessage', Immutable.Map()),
    room: state.getIn(['rooms', roomName])
  };
}

export default connect(mapStateToProps)(ChatBox);
