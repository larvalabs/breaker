import React, {Component} from 'react'
import Immutable from 'immutable'
import Config from '../config'
import Username from './Username'

export default class UserListItem extends Component {
  renderUserImage(){
    if(Config.features.noMessageAvatar.indexOf(this.props.roomName) > -1){
      return null
    }

    let userLink = `https://reddit.com/u/${this.props.user.get('username')}`;
    return <a href={userLink} className="pull-left thumb-sm avatar m-r" target="_blank">
      <img src={this.props.user.get('profileImageUrl')} alt="..." className="img-circle"/>
      <i className={ (this.props.user.get('online') ? 'on' : 'off' ) + " b-white bottom"} />
    </a>
  }
  renderStatusMessage(){
    if(Config.features.flairTextDescription.indexOf(this.props.roomName) > -1) {
      let flairText = this.props.user.getIn(['flair', this.props.roomName, 'flairText']);
      return <small className="text-muted">{flairText}</small>
    }
    let statusMessage = this.props.user.get('statusMessage') ? this.props.user.get('statusMessage') : '\u00a0';
    return <small className="text-muted">{statusMessage}</small>
  }
  render(){
    if(this.props.user.get('username') === "guest"){
      return null;
    }
    
    return <li key={this.props.user.get('id')} className="list-group-item">
      {this.renderUserImage()}
      <div className="clear">
        <Username user={this.props.user} roomName={this.props.roomName}
                  classOnly={Config.features.flairTextDescription.indexOf(this.props.roomName) > -1}/>
        {this.renderStatusMessage()}
      </div>
    </li>
  }
}

UserListItem.defaultProps = {
  user: Immutable.Map(),
  roomName: ""
};
