import React, {Component} from 'react'
var TimeAgo = require('react-timeago').default

export default class ChatMessage extends Component {
  renderUserImage() {
    let userLink = `https://reddit.com/u/${this.props.user.username}`;
    let profileImage = this.props.user.profileImageUrl;

    // TODO: seems like a hack here
    if (this.props.user.profileImageUrl.indexOf('user-anon') > -1){
      profileImage = '/public/img/user-empty.png';
    }

    return <a className="avatar thumb-sm pull-left m-r" href={userLink} target="_blank">
      <img src={profileImage} />
    </a>
  }
  renderTime() {
    // TODO: Timeago
    return <div className="pull-right text-sm text-muted">
      <TimeAgo date={new Date(this.props.message.createDateLongUTC).toISOString()} />
    </div>
  }
  renderUsername() {
    let modClass = this.props.user.modForRoom ? 'text-md text-primary-dker' : 'text-md text-dark-dker';
    return <div>
        <a className={modClass} href={`https://reddit.com/u/${this.props.user.username}`} target="_blank">
          {this.props.user.username}</a>
      </div>

  }
  renderMessage() {
    return <div className="m-t-midxs" dangerouslySetInnerHTML={{__html: this.props.message.messageHtml}}>
    </div>
  }
  renderLinks(){
    if(this.props.message.imageLinks && message.imageLinks.length > 0) {
      return <div className="m-t-sm">
        <a href={this.props.message.imageLinks[0]} target="_blank">
        <img src={this.props.message.imageLinks[0]} className="image-preview"/>
        </a>
      </div>;
    }

    return null;
  }
  render(){
    return <li className="list-group-item no-border p-t-s p-b-xs clearfix b-l-3x b-l-white">
      {this.renderUserImage()}
      {this.renderTime()}
      <div className="clear">
        {this.renderUsername()}
        {this.renderMessage()}
        {this.renderLinks()}
      </div>
    </li>
  }
}

ChatMessage.defaultProps = {
  message: {},
  user: {}
};
