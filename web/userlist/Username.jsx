import React, {Component} from 'react'
import Immutable from 'immutable'
import Config from '../config'


export default class Username extends Component {
  renderFlair() {
    let flairSettings = this.props.user.getIn(['flair', this.props.roomName]);
    if(!flairSettings){
      return null;
    }

    if(!flairSettings.get('flairCss') && !flairSettings.get('flairText')){
      return null;
    }

    let classes = `user-flair-${flairSettings.get('flairPosition', 'right')} flair flair-${flairSettings.get('flairCss')}`;

    if(!flairSettings.get('flairText') || this.props.classOnly){
      return <span className={classes} title={flairSettings.get('flairText')}></span>
    }
    
    

    return <span className={classes} title={flairSettings.get('flairText')}>{flairSettings.get('flairText')}</span>
  }
  render(){
    let modClass = this.props.user.get('modForRoom') ? 'text-md text-primary-dker' : 'text-md text-dark-dker';
    return <div className="message-container">
      <div className="flair-container">
        {this.renderFlair()}
      </div>
      <div className="username-container">
        <a className={modClass} href={`https://reddit.com/u/${this.props.user.get('username')}`} target="_blank">
          {this.props.user.get('username')}</a>
      </div>
    </div>
  }
}

