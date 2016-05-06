import React, {Component} from 'react'
import Immutable from 'immutable'
import Config from '../config'
import { connect } from 'react-redux'


class Message extends Component {
  constructor(props){
    super(props);
    this.renderFormattedMention = this.renderFormattedMention.bind(this);
  }
  renderFormattedMention(match, username){
    if(this.props.message.get('mentionedUsernames', Immutable.List()).contains(username.toLowerCase())){
      let classes = "username";
      if(username.toLowerCase() === this.props.authUser.get('username')){
        classes += " username-me"
      }
      return `<a target="_blank" class="${classes}" href=${"https://reddit.com/u/" + username}>${match}</a>`
    }

    return username
  }
  renderRawMessage() {
    return <div className="message-body m-t-midxs">
      <i>{this.props.message.get('message')}</i>
    </div>
  }
  renderHTMLMessage(){
    let classes = "message-body m-t-midxs";
    if (Config.features.useFlairStyle(this.props.roomName)) {
      classes += " flair-message-hack"
    }

    let message = this.props.message.get('messageHtml').replace(/@(\w+)/g, this.renderFormattedMention);
    return <div className={classes} dangerouslySetInnerHTML={{__html: message}}>
    </div>
  }
  renderMessageBody(){
    if(this.props.message.get('messageHtml')){
      return this.renderHTMLMessage()
    }
    return this.renderRawMessage()
  }
  renderLinks(){
    if(!this.props.message.get('imageLinks')){
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

function mapStateToProps(state) {
  return {
    authUser: state.getIn(['initial', 'user'])
  }
}


export default connect(mapStateToProps)(Message)
