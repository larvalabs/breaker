import React, {Component} from 'react';
import { connect } from 'react-redux';
import ChatMessage from './ChatMessage.jsx'
import ChatMessageInput from './ChatMessageInput.jsx'
import StayDown from 'staydown'
import Immutable from 'immutable'

class ChatBox extends Component {
  constructor(props) {
    super(props);
    this.onMessageInput = this.onMessageInput.bind(this);
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

  renderThreadMessage(props, message){
    return <ChatMessage message={message} user={props.users.get(message.get('username'))}/>
  }

  renderThread(props) {
    let filteredMessages = props.messages.filter((message) => message);

    return <ul id="thread" className="list-group list-group-lg no-radius m-b-none m-t-n-xxs">
      {filteredMessages.map((message) => this.renderThreadMessage(this.props, message))}
    </ul>
  }

  render() {
    return <div id="centercol" className="col">

      <div id="threadparent" className="vbox">
        <div className="row-row">
          <div id="thread_scrollparent" className="cell">
            <div className="cell-inner">

              {this.renderThread(this.props)}

              <div id="bottom-spacer" className="padder-v-sm bg-white b-l-3x b-l-white"></div>
            </div>
          </div>
        </div>

        <ChatMessageInput roomName={this.props.roomName}
                          onMessageInput={this.onMessageInput} />

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
    roomName: roomName
  }
}

export default connect(mapStateToProps)(ChatBox)
