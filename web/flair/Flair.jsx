import React, {Component} from 'react'
import Immutable from 'immutable'
import Config from '../config'
import { connect } from 'react-redux';


class Flair extends Component {
  hasText(flairSettings){
    return flairSettings.get('flairText') && flairSettings.get('flairText') !== "null"
  }
  hasCssClass(flairSettings){
    return flairSettings.get('flairCss') && flairSettings.get('flairCss') !== "null"
  }
  hasFlair(flairSettings){
    return this.hasCssClass(flairSettings) || this.hasText(flairSettings)
  }
  renderFlair() {
    let flairSettings = this.props.user.getIn(['flair', this.props.roomName]);
    if(!flairSettings){
      return null;
    }

    if(!this.hasFlair(flairSettings)){
      return null;
    }

    let classes = `flair flair-${flairSettings.get('flairCss')}`;

    if(!this.hasText(flairSettings) || this.props.classOnly){
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
