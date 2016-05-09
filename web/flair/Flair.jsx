import React, {Component} from 'react'
import Immutable from 'immutable'
import Config from '../config'
import { connect } from 'react-redux';


class Flair extends Component {
  renderFlair() {
    let flairSettings = this.props.user.getIn(['flair', this.props.roomName]);
    if(!flairSettings){
      return null;
    }

    if(!flairSettings.get('flairCss') && !flairSettings.get('flairText')){
      return null;
    }

    let classes = `flair flair-${flairSettings.get('flairCss')}`;

    if(!flairSettings.get('flairText') || this.props.classOnly){
      return <span className={classes} title={flairSettings.get('flairText')}></span>
    }
    
    return <span className={classes} title={flairSettings.get('flairText')}>{flairSettings.get('flairText')}</span>
  }
  render(){
      let flairScaleClass = Config.settings.flairScaleForRoom(this.props.room);
      return <div className={`flair-container ${flairScaleClass}`}>
        {this.renderFlair()}
      </div>
  }
}


function mapStateToProps(state) {
  let roomName = state.get('currentRoom');

  return {
    room: state.getIn(['rooms', roomName])
  }
}

export default connect(mapStateToProps)(Flair)
