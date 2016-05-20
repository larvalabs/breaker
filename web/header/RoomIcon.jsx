import React, {Component} from 'react'
import Immutable from 'immutable'


export default class RoomIcon extends Component {
  render(){
    if (this.props.room.get('iconUrl')){
      return <span className="m-t-xs m-r-sm floatleft">
            <img src={this.props.room.get('iconUrl')}  width="40" height="40"/>
        </span>
    } else {
      return null;
    }
  }
}

RoomIcon.defaultProps = {
  room: Immutable.Map()
};
