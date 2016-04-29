import React, {Component} from 'react';
import { connect } from 'react-redux';
import StayDown from 'staydown'
import Immutable from 'immutable'

import ChatThread from './ChatThread'
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
        <ChatThread messages={this.props.messages} users={this.props.users} roomName={this.props.roomName}/>
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
