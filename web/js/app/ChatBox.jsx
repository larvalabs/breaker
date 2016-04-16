import React, {Component} from 'react';
import { connect } from 'react-redux';
import ChatMessage from './ChatMessage.jsx'


class ChatBox extends Component {
  render() {
    return <div id="centercol" className="col">

      <div id="threadparent" className="vbox">
        <div className="row-row">
          <div id="thread_scrollparent" className="cell">
            <div className="cell-inner">

              <ul id="thread" className="list-group list-group-lg no-radius m-b-none m-t-n-xxs">
                {this.props.messages.filter((message) => message)
                    .map((message) => <ChatMessage message={message} user={this.props.users[message.username]}/>)}
              </ul>

              <div id="bottom-spacer" className="padder-v-sm bg-white b-l-3x b-l-white"></div>
            </div>
          </div>
        </div>

        <div className="padder padder-v b-t b-light text-center">
                            <textarea type="text" className="form-control input-message mention"
                                      placeholder="Type a message to ${roomName}..." />
        </div>

      </div>
    </div>
  }
}


ChatBox.defaultProps = {
  messages: []
};

function mapStateToProps(state) {
  var messages = [];
  if(state.messages.hasOwnProperty(state.initial.roomName)){
    messages = state.messages[state.initial.roomName]
  }

  console.log("messages", messages);
  return {
    messages: messages,
    users: state.users
  }
}

export default connect(mapStateToProps)(ChatBox)
