import React from 'react';
import SidebarUser from './SidebarUser.jsx'
import SidebarRoomListElm from './SidebarRoomListElm.jsx'
import Config from '../config.js'
import Immutable from 'immutable'

export default React.createClass({
  getDefaultProps: function(){
    return {
      activeRooms: Immutable.Map(),
      roomList: Immutable.Map(),
      roomName: null
    }
  },
  renderYourRooms: function(){
    return <ul id="roomlist" className="nav">
      <li key="your-rooms" className="hidden-folded padder m-t m-b-sm text-muted text-xs">
        <span>Your Rooms</span>
      </li>
      {
        this.props.roomList.toArray().map((room) => {
          return <SidebarRoomListElm room={room} active={room.get('name') == this.props.roomName}/>
        })
      }
    </ul>
  },
  renderSuggestedRooms: function(){
    if(!Config.features.suggestedRooms){
      return null;
    }

    if (this.props.activeRooms.length === 0 ) {
      return null
    }
  
    return <div>
      <li className="line dk"></li>
      <ul id="toproomlist" className="nav">
          <li className="hidden-folded padder m-t m-b-sm text-muted text-xs">
            <span>Suggested Active Rooms</span>
          </li>
          {this.props.activeRooms.map((room) => {
            return <SidebarRoomListElm room={room} active={false}/>
          })}
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
