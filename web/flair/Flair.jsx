import React, {Component} from 'react'
import Immutable from 'immutable'
import Config from '../config'


export default class Flair extends Component {
  renderFlair() {
    let flairSettings = this.props.user.getIn(['flair', this.props.roomName]);
    if(!flairSettings){
      console.log("settings empty", flairSettings);
      return null;
    }

    if(!flairSettings.get('flairCss') && !flairSettings.get('flairText')){
      console.log("flair empty", flairSettings);
      return null;
    }

    let classes = `flair flair-${flairSettings.get('flairCss')}`;

    if(!flairSettings.get('flairText') || this.props.classOnly){
      return <span className={classes} title={flairSettings.get('flairText')}></span>
    }
    
    return <span className={classes} title={flairSettings.get('flairText')}>{flairSettings.get('flairText')}</span>
  }
  render(){
      let flairScaleClass = Config.settings.flairScaleForRoom(this.props.roomName);
      return <div className={`flair-container ${flairScaleClass}`}>
        {this.renderFlair()}
      </div>
  }
}

