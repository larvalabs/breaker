import React, {Component} from 'react'
import Immutable from 'immutable'
import Config from '../config'
import Clear from '../util/Clear'

export default class ChatMessage extends Component {
  isServerMessage() {
    return this.props.message.get('type') === "servermessage"
  }
  renderUserImage() {
    if(Config.features.useFlairStyle(this.props.roomName)){
      return null;
    }

    let userLink = null;
    if(!this.isServerMessage()){
      userLink = `https://reddit.com/u/${this.props.user.get('username')}`;
    }

    return <a className="avatar thumb-sm pull-left m-r hidden-xs" href={userLink} target="_blank">
      &nbsp;
    </a>
  }
  renderServerMessage() {
    return <div className="message-body m-t-midxs">
      <i>{this.props.message.get('message')}</i>
    </div>
  }
  renderMessage() {
    if(this.isServerMessage()){
      return this.renderServerMessage()
    }

    let classes = "message-body m-t-midxs";
    if (Config.features.useFlairStyle(this.props.roomName)) {
      classes += " flair-message-hack"
    }
    return <div className={classes} dangerouslySetInnerHTML={{__html: this.props.message.get('messageHtml')}}>
    </div>
  }
  renderLinks(){
    if(this.isServerMessage()){
      return null
    }

    if(this.props.message.get('imageLinks', Immutable.List()).size > 0) {
      return <div className="m-t-sm">
        <a href={this.props.message.getIn(['imageLinks', 0])} target="_blank">
        <img src={this.props.message.getIn(['imageLinks', 0])} className="image-preview"/>
        </a>
      </div>;
    }

    return null;
  }
  render(){
    let liClasses = "chat-message-short list-group-item p-t-none p-b-xs no-border clearfix b-l-3x b-l-white";

    return (
      <li key={this.props.message.get('id')} className={liClasses}>
        {this.renderUserImage()}
        <Clear>
          {this.renderMessage()}
          {this.renderLinks()}
        </Clear>
      </li>
    )
  }
}

ChatMessage.defaultProps = {
  message: Immutable.Map(),
  user: Immutable.Map(),
};
