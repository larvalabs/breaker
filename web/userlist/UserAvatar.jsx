import React, {Component} from 'react'
import Config from '../config'


export default class UserAvatar extends Component {
  render(){
    if(Config.features.useFlairStyle(this.props.roomName)){
      return null
    }
    if(this.props.user.get('username') === "breakerbotsystem"){
      return null
    }
    let userImage = this.props.user.get('profileImageUrl');
    if(!userImage){
      userImage = "/public/img/user-anon.png";
    }
    let userLink = `https://reddit.com/u/${this.props.user.get('username')}`;
    return <a href={userLink} className="pull-left thumb-sm avatar m-r hidden-xs" target="_blank">
      <img src={userImage} alt="..." className="img-circle"/>
      <i className={ (this.props.user.get('online') ? 'on' : 'off' ) + " b-white bottom"} />
    </a>
  }
}

