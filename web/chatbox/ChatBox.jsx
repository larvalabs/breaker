import React, {Component} from 'react';
import { connect } from 'react-redux';
import StayDown from 'staydown'
import Immutable from 'immutable'

import ChatMessage from './ChatMessage'
import ChatShortMessage from './ChatShortMessage'
import ChatServerMessage from './ChatServerMessage'
import ChatMessageInput from './ChatMessageInput'
import ChatLoginInput from './ChatLoginInput'
import Config from '../config'


class ChatBox extends Component {
  constructor(props) {
    super(props);
    this.onMessageInput = this.onMessageInput.bind(this);
    this.renderMessageInput = this.renderMessageInput.bind(this);
  }

  componentDidMount() {
    var staydown = new StayDown({
      target: $('#thread_scrollparent')[0],
      interval: 500
    });

    this.setState({
      staydown: staydown
    });

    staydown.checkdown();
  }

  onMessageInput() {
    this.state.staydown.intend_down = true;
  }

  renderThreadMessage(props, message, previous){
    if(message.get('type') === "servermessage"){
      return <ChatServerMessage key={message.get('uuid')}
                                message={message}
                                roomName={props.roomName}/>
    }
    
    if(previous && previous.get('username') === message.get('username')){
      return <ChatShortMessage key={message.get('uuid')}
                               message={message}
                               user={props.users.get(message.get('username'))}
                               roomName={props.roomName}/>
    }

    return <ChatMessage key={message.get('uuid')}
                        message={message}
                        user={props.users.get(message.get('username'))}
                        roomName={props.roomName}/>
  }

  renderThread(props) {
    let filteredMessages = props.messages.filter((message) => message);

    return <ul id="thread" className="list-group list-group-lg no-radius m-b-none m-t-n-xxs">
      {
        filteredMessages.map((message, index) => {
          let previous = null;
          if(index > 0){
            previous = filteredMessages.get(index-1)
          }
          return this.renderThreadMessage(this.props, message, previous)
        })
      }
    </ul>
  }
  renderMessage(props){
    let body = props.message.get('body');
    let type = props.message.get('type');

    if(!body || !type){
      return null
    }
    return <div className={`message-box ${type}`}>{props.message.get('body')}</div>
  }
  renderMessageInput(){
    if(Config.guest){
      return <ChatLoginInput roomName={this.props.roomName} />
    } else {
      return <ChatMessageInput roomName={this.props.roomName} onMessageInput={this.onMessageInput}/>
    }
  }
  render() {
    return <div id="centercol" className="col">
      <div id="threadparent" className="vbox">
        {this.renderMessage(this.props)}
        <div className="row-row">
          <div id="thread_scrollparent" className="cell">
            <div className="cell-inner">

              {this.renderThread(this.props)}

              <div id="bottom-spacer" className="padder-v-sm bg-white b-l-3x b-l-white"></div>
            </div>
          </div>
        </div>
        <div className="padder padder-v b-t b-light text-center">
          {this.renderMessageInput()}
        </div>

      </div>
    </div>
  }
}


ChatBox.defaultProps = {
  messages: []
};

function mapStateToProps(state) {
  let roomName = state.getIn(['initial', 'roomName']);

  return {
    messages: state.getIn(['messages', roomName], Immutable.List()),
    users: state.get('users'),
    roomName: roomName,
    message: state.get('message', Immutable.Map())
  }
}

export default connect(mapStateToProps)(ChatBox)
