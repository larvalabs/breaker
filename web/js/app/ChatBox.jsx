import React, {Component} from 'react';
import { connect } from 'react-redux';
import ChatMessage from './ChatMessage.jsx'
import ChatMessageInput from './ChatMessageInput.jsx'
import StayDown from 'staydown'

class ChatBox extends Component {
  constructor(props) {
    super(props);
    this.onMessageInput = this.onMessageInput.bind(this);
  }

  componentDidMount() {
    // TODO: this needs to be imported
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

  render() {
    return <div id="centercol" className="col">

      <div id="threadparent" className="vbox">
        <div className="row-row">
          <div id="thread_scrollparent" className="cell">
            <div className="cell-inner">

              <ul id="thread" className="list-group list-group-lg no-radius m-b-none m-t-n-xxs">
                {this.props.messages.filter((message) => message)
                    .map((message) => <ChatMessage message={message}
                                                   user={this.props.users[message.username]} />)}
              </ul>

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
  let messages = [];
  let roomName = state.getIn(['initial', 'roomName']);
  if(state.getIn(['messages', roomName])){
    messages = state.getIn(['messages', roomName]).toJS()
  }

  return {
    messages: messages,
    users: state.get('users').toJS(),
    roomName: roomName
  }
}

export default connect(mapStateToProps)(ChatBox)
