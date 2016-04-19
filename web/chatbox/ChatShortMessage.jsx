import React, {Component} from 'react'
import Immutable from 'immutable'


export default class ChatMessage extends Component {
  renderUserImage() {
    let userLink = `https://reddit.com/u/${this.props.user.get('username')}`;

    return <a className="avatar thumb-sm pull-left m-r" href={userLink} target="_blank">
      &nbsp;
    </a>
  }
  renderMessage() {
    return <div className="m-t-midxs" dangerouslySetInnerHTML={{__html: this.props.message.get('messageHtml')}}>
    </div>
  }
  renderLinks(){
    if(this.props.message.get('imageLinks', Immutable.List()).length > 0) {
      return <div className="m-t-sm">
        <a href={this.props.message.getIn(['imageLinks', 0])} target="_blank">
        <img src={this.props.message.getIn(['imageLinks', 0])} className="image-preview"/>
        </a>
      </div>;
    }

    return null;
  }
  render(){
    let liClasses = "list-group-item p-t-none p-b-xs no-border clearfix b-l-3x b-l-white";

    return (
      <li key={this.props.message.get('id')} className={liClasses}>
        {this.renderUserImage()}
        <div className="clear">
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
};
