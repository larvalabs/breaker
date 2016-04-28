import React, {Component} from 'react'
import Immutable from 'immutable'
import Config from '../config'


export default class ChatMessage extends Component {
  renderUserImage() {
    if(Config.features.noMessageAvatar.indexOf(this.props.roomName) > -1){
      return null;
    }

    return <a className="avatar thumb-sm pull-left m-r hidden-xs" target="_blank">&nbsp;</a>
  }
  renderMessage() {
    return <div className="message-body m-t-midxs">
      <i>{this.props.message.get('message')}</i>
    </div>
  }
  render(){
    let liClasses = "chat-message-server list-group-item no-border p-t-s p-b-xs clearfix b-l-3x b-l-white";

    return (
      <li className={liClasses}>
        {this.renderUserImage()}
        <div className="clear">
          {this.renderMessage()}
        </div>
      </li>
    )
  }
}

ChatMessage.defaultProps = {
  message: Immutable.Map(),
  user: Immutable.Map(),
  root: true
};
