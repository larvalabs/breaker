import React, {Component} from 'react'
import Immutable from 'immutable'
import TimeAgo from 'react-timeago'
import Config from '../config'
import Username from '../userlist/Username.jsx'

export default class ChatMessage extends Component {
  renderUserImage() {
    if(Config.features.noMessageAvatar.indexOf(this.props.roomName) > -1){
      return null;
    }
    
    let userLink = `https://reddit.com/u/${this.props.user.get('username')}`;
    let profileImage = this.props.user.get('profileImageUrl');

    // TODO: seems like a hack here
    if (profileImage && profileImage.indexOf('user-anon') > -1){
      profileImage = '/public/img/user-anon.png';
    }

    return <a className="avatar thumb-sm pull-left m-r hidden-xs" href={userLink} target="_blank">
      <img src={profileImage} />
    </a>
  }
  renderTime() {
    return <div className="pull-right text-sm hidden-xs text-muted">
      <TimeAgo date={new Date(this.props.message.get('createDateLongUTC')).toISOString()} />
    </div>
  }
  renderMessage() {
    let classes = "message-body m-t-midxs";
    if (Config.features.noMessageAvatar.indexOf(this.props.roomName) > -1) {
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
    let liClasses = "chat-message-root list-group-item no-border p-t-s p-b-xs clearfix b-l-3x b-l-white";

    return (
      <li className={liClasses}>
        {this.renderUserImage()}
        {this.renderTime()}
        <div className="clear">
          <Username user={this.props.user} roomName={this.props.roomName} />
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
