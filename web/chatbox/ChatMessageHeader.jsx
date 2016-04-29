import React, {Component} from 'react'
import Immutable from 'immutable'
import UsernameAndFlair from '../userlist/UsernameAndFlair.jsx'
import UserAvatar from '../userlist/UserAvatar'
import Clear from '../util/clear'
import ChatMessageTimeago from './ChatMessageTimeago'

export default class ChatMessage extends Component {
  render(){
    if(this.props.previous && this.props.previous.get('username') === this.props.message.get('username')){
      return null
    }
    let liClasses = "chat-message-root list-group-item no-border p-t-s p-b-xs clearfix b-l-3x b-l-white";

    return (
      <li className={liClasses}>
        <UserAvatar user={this.props.user} roomName={this.props.roomName}/>
        <ChatMessageTimeago time={this.props.message.get('createDateLongUTC')} />
        <Clear>
          <UsernameAndFlair user={this.props.user} roomName={this.props.roomName} />
        </Clear>
      </li>
    )
  }
}

ChatMessage.defaultProps = {
  message: Immutable.Map(),
  user: Immutable.Map(),
  root: true
};
