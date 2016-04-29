import React, {Component} from 'react'
import UsernameAndFlair from './UsernameAndFlair'
import UserAvatar from './UserAvatar'
import UserStatusMessage from './UserStatusMessage'
import Immutable from 'immutable'
import Clear from '../util/Clear'

export default class UserListItem extends Component {
  render(){
    if(this.props.user.get('username') === "guest"){
      return null;
    }
    
    return <li key={this.props.user.get('id')} className="list-group-item">
      <UserAvatar user={this.props.user} roomName={this.props.roomName}/>
      <Clear>
        <UsernameAndFlair user={this.props.user} roomName={this.props.roomName} />
        <UserStatusMessage user={this.props.user} roomName={this.props.roomName} />
      </Clear>
    </li>
  }
}

UserListItem.defaultProps = {
  user: Immutable.Map(),
  roomName: ""
};
