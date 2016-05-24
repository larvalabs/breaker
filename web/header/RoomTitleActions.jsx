import React, {Component} from 'react'
import { connect } from 'react-redux' 
import Immutable from 'immutable'
import {handleLeaveRoom} from '../redux/actions/menu-actions'

import Config from '../config'

class RoomTitleActions extends Component {
  renderLeaveRoom() {
    if(Config.guest){
      return null;
    }

    return <span>| <a href="#" onClick={this.props.handleLeaveRoom}>leave</a></span>
  }
  renderEditRoom() {
    if(!this.props.userIsMod && !Config.admin){
      return null;
    }

    return <span> | <a id="room-pref" href={`/roommanage/roomprefs?roomName=${this.props.room.get('name')}`}>edit</a></span>
  }
  render(){
    if(!this.props.room.get('name')){
      return null;
    }

    return <span className="room-options">
      {this.renderLeaveRoom()} {this.renderEditRoom()}
    </span>
  }
}

RoomTitleActions.defaultProps = {
  room: Immutable.Map(),
  userIsMod: false
};


function mapStateToProps(state) {
  return {}
}

function mapDispatchToProps(dispatch, ownProps) {
  return {
    handleLeaveRoom(){
      if(Config.guest){
        return null;
      }
      dispatch(handleLeaveRoom(ownProps.room.get('name')))
    }
  }
}

export default connect(mapStateToProps, mapDispatchToProps)(RoomTitleActions)
