import React, {Component} from 'react';
import { connect } from 'react-redux';
import {sendNewMessage} from '../redux/actions/chat-actions'


class ChatMessageInput extends Component {
  constructor(props) {
    super(props);
    this.handleKeyPress = this.handleKeyPress.bind(this);
    this.handleChange = this.handleChange.bind(this);
    this.state = {
      messageText: undefined
    }
  }

  handleKeyPress(event) {
    if(event.key == 'Enter'){
      event.preventDefault();
      this.props.dispatch(sendNewMessage({
        message: event.target.value,
        roomName: this.props.roomName
      }));
      this.setState({
        messageText: ""
      });
      this.props.onMessageInput();
    }
  }
  handleChange(event) {
    this.setState({messageText: event.target.value})
  }
  render() {
    return <div className="padder padder-v b-t b-light text-center">
        <textarea type="text"
                  style={{resize: "none"}}
                  className="form-control input-message mention"
                  placeholder={`Type a message to ${this.props.roomName}...`}
                  onKeyPress={this.handleKeyPress}
                  onChange={this.handleChange}
                  value={this.state.messageText} />
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
