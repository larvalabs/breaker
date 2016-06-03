import React, { Component } from 'react';
import { connect } from 'react-redux';
import Immutable from 'immutable';

import Config from '../config';
import StayDown from 'staydown';

import ChatThread from './ChatThread';
import ChatMessageInput from './ChatMessageInput';
import ChatLoginInput from './ChatLoginInput';

import { getCurrentRoom } from '../redux/selectors/rooms-selectors';

const $ = window.jQuery;


class Chat extends Component {
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

  renderMessage() {
    const { message } = this.props;
    const body = message.get('body');
    const type = message.get('type');

    if (!body || !type) {
      return null;
    }

    return <div className={`message-box ${type}`}>{body}</div>;
  }
  renderMessageInput() {
    const { room, roomName } = this.props;

    if (Config.guest) {
      return <ChatLoginInput roomName={roomName} room={room}/>;
    }

    return <ChatMessageInput roomName={roomName} onMessageInput={this.onMessageInput}/>;
  }
  render() {
    const { users, roomName } = this.props;
    return (
      <div id="centercol" className="col">
        <div id="threadparent" className="vbox">
          {this.renderMessage()}
          <ChatThread users={users} roomName={roomName}/>
          <div className="input-container padder padder-v b-t b-light text-center">
            {this.renderMessageInput()}
          </div>
        </div>
      </div>
    );
  }
}

Chat.defaultProps = {
  messages: []
};

function mapStateToProps(state) {
  return {
    users: state.get('users'),
    roomName: state.get('currentRoom'),
    message: state.get('bannerMessage', Immutable.Map()),
    room: getCurrentRoom(state)
  };
}

export default connect(mapStateToProps)(Chat);
