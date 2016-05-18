import React from "react";
import { connect } from 'react-redux'
import Immutable from 'immutable'
import Config from '../config'
import {toggleSidebar, toggleSettings} from '../redux/actions/menu-actions'

var Header = React.createClass({
  getDefaultProps: function() {
    return {
      user: Immutable.Map(),
      room: Immutable.Map(),
      roomName: null,
      userIsMod: false
    }
  },
  onSidebarButtonClick: function(){
    this.props.dispatch(toggleSidebar());
  },
  onSettingsButtonClick: function(){
    this.props.dispatch(toggleSettings());
  },
  renderLogin: function(){
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
  },
  renderUsername: function(){
    if(Config.features.useFlairStyle(this.props.roomName)){
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
  },
  renderProfileMenu: function(){
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
  },
  renderSettingsCog: function(){
    let classes = "pull-right visible-xs";
    if(this.props.settings_open){
      classes += " active";
    }
    let iconColor = this.props.room.getIn(['styles', 'sidebarTextColor'], "#EAEBED");
    let iconBGColor = this.props.room.getIn(['styles', 'sidebarBackgroundColor'], "#3a3f51");

    return  <button className={classes}
                    ui-toggle-className="show"
                    target=".navbar-collapse"
                    onClick={this.onSettingsButtonClick}
                    style={{backgroundColor: iconBGColor}}>
      
      <i className="glyphicon glyphicon-cog" style={{color: iconColor}}/>
    </button>
  },
  renderSidebarHamburger: function(){
    let classes = "pull-right visible-xs";
    let active = this.props.sidebar_open ? " active" : "";
    let iconColor = this.props.room.getIn(['styles', 'sidebarTextColor'], "#EAEBED");
    let iconBGColor = this.props.room.getIn(['styles', 'sidebarBackgroundColor'], "#3a3f51");
    return <button className={classes + active} ui-toggle-className="off-screen" target=".app-aside" ui-scroll="app" onClick={this.onSidebarButtonClick}>
      <i className="glyphicon glyphicon-align-justify" style={{color: iconColor, background: iconBGColor}}/>
    </button>
  },
  renderUnreadCount(props){
      let backgroundColor = props.room.getIn(['styles', 'sidebarUnreadColor']);
      let textColor = props.room.getIn(['styles', 'sidebarUnreadTextColor']);
      if(this.props.unreadCount > 0) {
        return <div className="unread-count-total pull-right"><b className="unreadcount label bg-info pull-right"
                  style={{backgroundColor: backgroundColor, color: textColor}}>{this.props.unreadCount}</b></div>
      }
      return null;
  },
  renderBreakerLogoBox: function() {
    let styles = Config.styles.getSidebarColorForRoom(this.props.room);
    let sidebarColor = this.props.room.getIn(['styles', 'sidebarTextColor']);

    return <div className="navbar-header bg-dark" style={styles}>
      {this.renderUnreadCount(this.props)}
      {this.renderSettingsCog()}
      {this.renderSidebarHamburger()}
      <a href="#" className="navbar-brand text-lt" style={styles}>
        <i className="fa fa-terminal" style={{color: sidebarColor}}/>
        <span className="hidden-folded m-l-xs" style={{marginLeft: "10px", color: sidebarColor}}>breaker</span>
      </a>
    </div>
  },
  renderRoomTitle: function() {
    if(!this.props.room.get('name')){
      return null;
    }
    return <ul className="nav navbar-nav hidden-sm" >

      <li className="m-t-xs m-b-xxs middle" >
        <span id="room-title" className="h4 m-n font-thin h4 text-black">
          <a href={"https://reddit.com/r/" + this.props.roomName} target="_blank">#{this.props.roomName}</a></span>
        {this.renderModCustomize()}
        <br/>
        <small id="room-modmessage" className="text-muted">
          {this.props.room.get('banner') ? this.props.room.get('banner') : Config.settings.default_banner}
        </small>
      </li>

    </ul>
  },
  renderModCustomize: function () {
    if(!this.props.room.get('name')){
      return null;
    }

    if(!this.props.userIsMod && !Config.admin){
      return null;
    }
    return <span>
      <a id="room-pref" href={`/roommanage/roomprefs?roomName=${this.props.roomName}`}> (customize)</a>
    </span>
  },
  renderRoomIcon: function() {
    if (this.props.room.get('iconUrl')){
      return <span className="m-t-xs m-r-sm floatleft">
            <img src={this.props.room.get('iconUrl')}  width="40" height="40"/>
        </span>
    } else {
      return null;
    }
  },
  render: function () {
    let classes = "collapse pos-rlt navbar-collapse box-shadow bg-white-only";
    if(this.props.settings_open){
      classes += " show";
    }
    return <header className="app-header navbar" role="menu">
      {this.renderBreakerLogoBox()}
      <div className={classes}>
        {this.renderRoomIcon()}
        {this.renderRoomTitle()}
        {Config.guest ? this.renderLogin() : this.renderProfileMenu()}
      </div>
    </header>
  }
});

function mapStateToProps(state) {
  return {
    sidebar_open: state.getIn(['ui', 'sidebar_open'], false),
    settings_open: state.getIn(['ui', 'settings_open'], false),
  }
}

export default connect(mapStateToProps)(Header)
