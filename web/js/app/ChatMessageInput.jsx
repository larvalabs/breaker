import React, {Component} from 'react';
import { connect } from 'react-redux';
import {sendNewMessage} from '../redux/actions/chat-actions.js'

class ChatMessageInput extends Component {
  constructor(props) {
    super(props);
    this.handleKeyPress = this.handleKeyPress.bind(this);
  }

  handleKeyPress(event) {
    if(event.key == 'Enter'){
      console.log('enter press here! ', event.target.value);
      event.preventDefault();
      this.props.dispatch(sendNewMessage({
        message: event.target.value,
        roomName: this.props.roomName
      }));

      this.props.onMessageInput();
    }
  }
  render() {
    return <div className="padder padder-v b-t b-light text-center">
        <textarea type="text"
                  style={{resize: "none"}}
                  className="form-control input-message mention"
                  placeholder={`Type a message to ${this.props.roomName}...`}
                  onKeyPress={this.handleKeyPress}/>
    </div>
  }
}

ChatMessageInput.defaultProps = {
  roomName: null
};

function mapStateToProps(state) {
  return {
  }
}

export default connect(mapStateToProps)(ChatMessageInput)
