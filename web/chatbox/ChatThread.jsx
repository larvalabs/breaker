import React, { Component } from 'react';
import { connect } from 'react-redux';
import Immutable from 'immutable';
import ReactDOM from 'react-dom';

import Config from '../config';

import ChatMessageHeader from './ChatMessageHeader';
import ChatShortMessage from './ChatShortMessage';
import ChatMessage from './ChatMessage';
import MessageHistory from './MessageHistory';

import * as scrollActions from '../redux/actions/scroll-actions';


class ChatThread extends Component {
  constructor(props) {
    super(props);
    this.handleOnScroll = this.handleOnScroll.bind(this);
  }

  componentDidUpdate() {
    if (this.props.scrollToMessageId) {
      const thread = ReactDOM.findDOMNode(this.refs.thread);
      const scrollTo = ReactDOM.findDOMNode(this.refs[this.props.scrollToMessageId]);
      thread.scrollTop = scrollTo.offsetTop;
      this.props.resetScrollToMessageId();
    }
  }

  handleOnScroll(event) {
    if (event.nativeEvent.srcElement && event.nativeEvent.srcElement.scrollTop === 0) {
      this.props.handleMoreMessages();
    }
  }

  shouldRenderShortMessage(previousMessage, currentMessage) {
    if (!previousMessage) {
      return false;
    }

    const previousTime = previousMessage.get('createDateLongUTC');
    const currentTime = currentMessage.get('createDateLongUTC');

    const userIsTheSame = previousMessage.get('username') === currentMessage.get('username');
    const timeUnderAllowedBreak = currentTime - previousTime < Config.settings.message_split_millis;
    return userIsTheSame && timeUnderAllowedBreak;
  }

  renderThreadMessageNewWay(props, message, previous) {
    return (
      <div key={message.get('uuid')}>
        <ChatMessageHeader message={message}
                           ref={previous ? null : 'first_message'}
                           previous={previous}
                           user={props.users.get(message.get('username'))}
                           roomName={props.roomName}
        />
        <ChatShortMessage message={message}
                          user={props.users.get(message.get('username'))}
                          roomName={props.roomName}
        />
      </div>
    );
  }

  renderThreadMessageOldWay(props, currentMessage, previousMessage) {
    if (currentMessage.get('type') === 'first_sentinel') {
      return null;
    }

    if (this.shouldRenderShortMessage(previousMessage, currentMessage)) {
      return (
        <ChatShortMessage key={currentMessage.get('uuid')}
                          ref={currentMessage.get('uuid')}
                          message={currentMessage}
                          user={props.users.get(currentMessage.get('username'))}
                          roomName={props.roomName}
        />
      );
    }

    return (
        <ChatMessage key={currentMessage.get('uuid')}
                     ref={currentMessage.get('uuid')}
                     message={currentMessage}
                     user={props.users.get(currentMessage.get('username'))}
                     roomName={props.roomName}
        />
    );
  }

  renderThreadMessage(props, message, previous) {
    if (Config.features.useFlairStyle(this.props.roomName)) {
      return this.renderThreadMessageNewWay(props, message, previous);
    }

    return this.renderThreadMessageOldWay(props, message, previous);
  }

  renderThread(props) {
    const filteredMessages = props.messages.filter((message) => message);

    return (
      <ul id="thread" className="list-group list-group-lg no-radius m-b-none m-t-n-xxs">
        <MessageHistory />
        {
          filteredMessages.map((message, index) => {
            let previous = null;
            if (index > 0) {
              previous = filteredMessages.get(index - 1);
            }
            return this.renderThreadMessage(this.props, message, previous);
          })
        }
      </ul>
    );
  }
  render() {
    return (
      <div className="row-row">
        <div ref="thread" id="thread_scrollparent" className="cell" onScroll={this.handleOnScroll}>
          <div className="cell-inner">
            {this.renderThread(this.props)}
            <div id="bottom-spacer" className="padder-v-sm bg-white b-l-3x b-l-white"></div>
          </div>
        </div>
      </div>
    );
  }
}

function mapStateToProps(state) {
  const roomName = state.get('currentRoom');
  const messages = state.getIn(['roomMessages', roomName], Immutable.List()).map((uuid) => state.getIn(['messages', uuid]));
  const firstMessage = messages.first();

  let firstMessageID = null;
  if (firstMessage) {
    firstMessageID = firstMessage.get('uuid');
  }

  return {
    scrollToMessageId: state.getIn(['ui', 'scrollToMessageId']),
    room: state.getIn(['rooms', roomName]),
    users: state.get('users'),
    firstMessageID,
    messages,
    roomName
  };
}

function mapDispatchToProps(dispatch) {
  return {
    handleMoreMessages() {
      dispatch(scrollActions.handleMoreMessages());
    },
    resetScrollToMessageId(){
      dispatch(scrollActions.resetScrollToMessage());
    }
  };
}

export default connect(mapStateToProps, mapDispatchToProps)(ChatThread);
