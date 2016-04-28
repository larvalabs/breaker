import React, {Component} from 'react'
import Immutable from 'immutable'
import TimeAgo from 'react-timeago'


export default class ChatMessage extends Component {
  renderUserImage() {
    let userLink = `https://reddit.com/u/${this.props.user.get('username')}`;
    let profileImage = this.props.user.get('profileImageUrl');

    // TODO: seems like a hack here
    if (profileImage && profileImage.indexOf('user-anon') > -1){
      profileImage = '/public/img/user-anon.png';
    }

    return <a className="avatar thumb-sm pull-left m-r" href={userLink} target="_blank">
      <img src={profileImage} />
    </a>
  }
  renderTime() {
    return <div className="pull-right text-sm hidden-xs text-muted">
      <TimeAgo date={new Date(this.props.message.get('createDateLongUTC')).toISOString()} />
    </div>
  }
  renderFlair() {
    let flairSettings = this.props.user.getIn(['flair', this.props.roomName]);
    if(!flairSettings){
      return null;
    }

    if(!flairSettings.get('flairCss') && !flairSettings.get('flairText')){
      return null;
    }

    let classes = `user-flair-${flairSettings.get('flairPosition', 'right')} flair flair-${flairSettings.get('flairCss')}`;

    if(!flairSettings.get('flairText')){
      return <span className={classes} title={flairSettings.get('flairText')}></span>
    }

    return <span className={classes} title={flairSettings.get('flairText')}>{flairSettings.get('flairText')}</span>
  }
  renderUsername() {
    let modClass = this.props.user.get('modForRoom') ? 'text-md text-primary-dker' : 'text-md text-dark-dker';
    return <div className="message-container">
        <a className={modClass} href={`https://reddit.com/u/${this.props.user.get('username')}`} target="_blank">
          {this.props.user.get('username')}</a>
      {this.renderFlair()}
      </div>

  }
  renderMessage() {
    return <div className="message-body m-t-midxs" dangerouslySetInnerHTML={{__html: this.props.message.get('messageHtml')}}>
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
          {this.renderUsername()}
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
