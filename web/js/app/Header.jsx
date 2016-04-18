import React from "react";
import Immuable from 'immutable'

export default React.createClass({
  getDefaultProps: function() {
    return {
      user: Immuable.Map(),
      room: {
        icon: null
      },
      roomName: null
    }
  },
  renderProfileMenu: function(){
    return <ul className="nav navbar-nav navbar-right">

      <li className="dropdown v-middle">
        <a href="#" data-toggle="dropdown" className="dropdown-toggle clear" data-toggle="dropdown">
          <span className="thumb-sm avatar pull-right m-t-n-sm m-b-n-sm m-l-sm">
            <img src={this.props.user.get('profileImageUrl')} alt="..." />
              <i className="on md b-white bottom" />
          </span>
          <span className="hidden-sm hidden-md">{this.props.user.get('username')}</span> <b className="caret" />
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
  },
  renderBreakerLogoBox: function() {
    return <div className="navbar-header bg-dark">
      <button className="pull-right visible-xs dk" ui-toggle-className="show" target=".navbar-collapse">
        <i className="glyphicon glyphicon-cog" />
      </button>
      <button className="pull-right visible-xs" ui-toggle-className="off-screen" target=".app-aside" ui-scroll="app">
        <i className="glyphicon glyphicon-align-justify"/>
      </button>
      <a href="#" className="navbar-brand text-lt">
        <i className="fa fa-terminal"/>
        <span className="hidden-folded m-l-xs">breaker</span>
      </a>
    </div>
  },
  renderRoomTitle: function() {
    return <ul className="nav navbar-nav hidden-sm" >

      <li className="m-t-xs m-b-xxs middle" >
        <span id="room-title" className="h4 m-n font-thin h4 text-black">
          <a href={"https://reddit.com/r/" + this.props.roomName} target="_blank">#{this.props.roomName}</a></span>
        {this.renderModCustomize()}
        <br/>
        <small id="room-modmessage" className="text-muted">
          Message from the moderators to you, the user.
        </small>
      </li>

    </ul>
  },
  renderModCustomize: function () {
    if(!this.props.room.isUserModerator){
      return null;
    }
    return <span>
      <a id="room-pref" className="hidden">(customize)</a>
    </span>
  },
  renderRoomIcon: function() {
    if (this.props.room.icon){
      return <span className="m-t-xs m-r-sm floatleft">
            <img id="room-icon" width="40" height="40"/>
        </span>
    } else {
      return null;
    }
  },
  render: function () {
    return <header id="header" className="app-header navbar" role="menu">

      {this.renderBreakerLogoBox()}

      <div className="collapse pos-rlt navbar-collapse box-shadow bg-white-only">
        {this.renderRoomIcon()}
        {this.renderRoomTitle()}
        {this.renderProfileMenu()}
      </div>
    </header>
  }
})
