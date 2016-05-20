import React, {Component} from 'react'
import Immutable from 'immutable'
import Config from '../config'

export default class HeaderUserMenu extends Component {
  renderUsername(){
    if(Config.features.useFlairStyle(this.props.room.get('name'))){
      return <div>
        <span className="hidden-sm hidden-md">{this.props.user.get('username')}</span> <b className="caret" />
      </div>;
    }
    return <div><span className="thumb-sm avatar pull-right m-t-n-sm m-b-n-sm m-l-sm">
            <img src={this.props.user.get('profileImageUrl')} alt="..." />
              <i className="on md b-white bottom" />
          </span>
      <span className="hidden-sm hidden-md">{this.props.user.get('username')}</span> <b className="caret" />
    </div>
  }
  renderProfileMenu(){
    return <ul className="nav navbar-nav navbar-right">

      <li className="dropdown v-middle">
        <a href="#" data-toggle="dropdown" className="dropdown-toggle clear" data-toggle="dropdown">
          {this.renderUsername()}
        </a>
        <ul className="dropdown-menu w">

          <li>
            <a ui-sref="access.signin" href="/usermanage/prefs">Preferences</a>
          </li>
          <li>
            <a ui-sref="access.signin" href="/logout">Logout</a>
          </li>
        </ul>
      </li>
    </ul>
  }
  renderLogin(){
    let signInUrl = `/application/startauthforguest?roomName=${this.props.roomName}`;
    if(window.innerWidth < 850){
      signInUrl += "&compact=true"
    }
    return <ul className="nav navbar-nav navbar-right">
      <li style={{textAlign: "center", paddingRight: "1em", fontWeight: "600"}}>
        <a href={signInUrl}>
          Sign in
        </a>
      </li>
    </ul>
  }
  render(){
    if(Config.guest) {
      return this.renderLogin();
    }

    return this.renderProfileMenu()
  }
}

HeaderUserMenu.defaultProps = {
  room: Immutable.Map(),
  user: Immutable.Map()
};
