import React, {Component} from 'react'
import Immutable from 'immutable'
import Config from '../config'

export default class Message extends Component {
  constructor(props){
    super(props);
    this.renderFormattedMention = this.renderFormattedMention.bind(this);
  }
  isServerMessage() {
    return this.props.message.get('type') === "servermessage"
  }
  renderServerMessage() {
    return <div className="message-body m-t-midxs">
      <i>{this.props.message.get('message')}</i>
    </div>
  }
  renderFormattedMention(match, username){
    if(this.props.message.get('mentionedUsernames', Immutable.List()).contains(username.toLowerCase())){
      return `<a target="_blank" class="username" href=${"https://reddit.com/u/" + username}>${match}</a>`
    }

    return username
  }
  renderUserMessage(){
    let classes = "message-body m-t-midxs";
    if (Config.features.useFlairStyle(this.props.roomName)) {
      classes += " flair-message-hack"
    }

    let message = this.props.message.get('messageHtml').replace(/@(\w+)/g, this.renderFormattedMention);
    return <div className={classes} dangerouslySetInnerHTML={{__html: message}}>
    </div>
  }
  renderMessageBody(){
    if(this.isServerMessage()){
      return this.renderServerMessage()
    }

    return this.renderUserMessage()
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
    return <div>
      {this.renderMessageBody()}
      {this.renderLinks()}
    </div>
  }
}

Message.defaultProps = {
  message: Immutable.Map(),
  user: Immutable.Map(),
  root: true
};
