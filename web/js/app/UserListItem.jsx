import React, {Component} from 'react'

export default class UserListItem extends Component {

  render(){
    console.log("member", this.props.user);
    return <li className="list-group-item">
      <a href={"https://reddit.com/u/" + this.props.user.username} className="pull-left thumb-sm avatar m-r" target="_blank">
        <img src={this.props.user.profileImageUrl} alt="..." className="img-circle"/>
        <i className={ (this.props.user.online ? 'on' : 'off' ) + " b-white bottom"} />
      </a>
      <div className="clear">
        <div className={this.props.user.modForRoom ? 'text-primary-dker' : ''}>
          <a href={"https://reddit.com/u/" + this.props.user.username} target="_blank">{this.props.user.username}</a>
        </div>
        <small className="text-muted">{this.props.user.statusMessage ? this.props.user.statusMessage : ''}</small>
      </div>
    </li>
  }
}

UserListItem.defaultProps = {
  user: {}
};
