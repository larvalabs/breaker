import React, {Component} from 'react'
import Immutable from 'immutable'
import TimeAgo from 'react-timeago'
import Config from '../config'
import UsernameAndFlair from '../userlist/UsernameAndFlair.jsx'
import UserAvatar from '../userlist/UserAvatar'

export default class ChatMessage extends Component {
  renderTime() {
    return <div className="pull-right text-sm hidden-xs text-muted">
      <TimeAgo date={new Date(this.props.message.get('createDateLongUTC')).toISOString()} />
    </div>
  }
  renderMessage() {
    let classes = "message-body m-t-midxs";
    if (Config.features.useFlairStyle(this.props.roomName)) {
      classes += " flair-message-hack"
    }
    return <div className={classes} dangerouslySetInnerHTML={{__html: this.props.message.get('messageHtml')}}>
    </div>
  }
  renderLinks(){
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
    let liClasses = "chat-message-root-old list-group-item no-border p-t-s p-b-xs clearfix b-l-3x b-l-white";

    return (
      <li className={liClasses}>
        <UserAvatar user={this.props.user} roomName={this.props.roomName} />
        {this.renderTime()}
        <div className="clear">
          <UsernameAndFlair user={this.props.user} roomName={this.props.roomName}
                            messageUsername={this.props.message.get('username')}/>
          {this.renderMessage()}
          {this.renderLinks()}
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
