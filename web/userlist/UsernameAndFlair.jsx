import React, {Component} from 'react'
import Flair from '../flair/Flair'


export default class UsernameAndFlair extends Component {
  renderFlairIfNecessary() {
    if(this.props.noFlair){
      return null;
    }
    return <Flair user={this.props.user} roomName={this.props.roomName} classOnly={this.props.classOnly}/>
  }
  render(){
    let modClass = this.props.user.get('modForRoom') ? 'text-md text-primary-dker' : 'text-md text-dark-dker';
    return <div className="message-container">
      
      <div className="username-container">
        <a className={modClass} href={`https://reddit.com/u/${this.props.user.get('username')}`} target="_blank">
          {this.props.user.get('username')}</a>
      </div>
      {this.renderFlairIfNecessary()}
    </div>
  }
}

