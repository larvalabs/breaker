import React from 'react';
import SidebarUser from './SidebarUser.jsx'

export default React.createClass({
  getDefaultProps: function(){
    return {
      activeRooms: []
    }
  },
  renderYourRooms: function(){
    return <ul id="roomlist" className="nav">
      <li className="hidden-folded padder m-t m-b-sm text-muted text-xs">
        <span>Your Rooms</span>
      </li>
    </ul>
  },
  renderSuggestedRooms: function(){
    if (this.props.activeRooms.length === 0 ) {
      return null
    }
  
    return <div>
      <li className="line dk"></li>

      <ul id="toproomlist" className="nav">
          <li className="hidden-folded padder m-t m-b-sm text-muted text-xs">
            <span>Suggested Active Rooms</span>
          </li>
  
          <li className="roomlistentry">
            {this.props.activeRooms.map((room) => {
              return <a href={"/d/" + room.name}>
                <img className="roomIconSmall" src={room.iconUrl != null ? room.iconUrl : '/public/images/blank.png' }/>
                <b className="unreadcount label bg-info pull-right" />
                <span className="roomname">#{room.name}</span>
              </a>
            })}

          </li>
      </ul>
    </div>
  },
  render: function () {
    return (
      <aside id="aside" className="app-aside hidden-xs bg-dark">
        <div className="aside-wrap">
          <div className="navi-wrap">

            <SidebarUser />

            <nav ui-nav className="navi clearfix">
              {this.renderYourRooms()}
              {this.renderSuggestedRooms()}
            </nav>

          </div>
        </div>
      </aside>
    )
  }
})
