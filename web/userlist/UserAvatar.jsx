import React, {Component} from 'react'
import Config from '../config'


export default class UserAvatar extends Component {
  render(){
    if(Config.features.useFlairStyle(this.props.roomName)){
      return null
    }

    let userLink = `https://reddit.com/u/${this.props.user.get('username')}`;
    return <a href={userLink} className="pull-left thumb-sm avatar m-r" target="_blank">
      <img src={this.props.user.get('profileImageUrl')} alt="..." className="img-circle"/>
      <i className={ (this.props.user.get('online') ? 'on' : 'off' ) + " b-white bottom"} />
    </a>
  }
}

