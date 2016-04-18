import React, {Component} from 'react'
import Immutable from 'immutable'


export default class UserListItem extends Component {

  render(){
    let userLink = `https://reddit.com/u/${this.props.user.get('username')}`;
    let statusMessage = this.props.user.get('statusMessage') ? this.props.user.get('statusMessage') : '\u00a0';

    return <li key={this.props.user.get('id')} className="list-group-item">
      <a href={userLink} className="pull-left thumb-sm avatar m-r" target="_blank">
        <img src={this.props.user.get('profileImageUrl')} alt="..." className="img-circle"/>
        <i className={ (this.props.user.get('online') ? 'on' : 'off' ) + " b-white bottom"} />
      </a>
      <div className="clear">
        <div className={this.props.user.get('modForRoom') ? 'text-primary-dker' : ''}>
          <a href={userLink} target="_blank">{this.props.user.get('username')}</a>
        </div>
        <small className="text-muted">{statusMessage}</small>
      </div>
    </li>
  }
}

UserListItem.defaultProps = {
  user: Immutable.Map()
};
