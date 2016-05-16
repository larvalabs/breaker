import React, {Component} from 'react'
import Config from '../config'

export default class SidebarLeaveRoom extends Component {
  constructor(props){
    super(props);
    this.getStyles = this.getStyles.bind(this);
  }
  getStyles(){
    let color = this.props.styles.get('sidebarTextColor');
    let display = this.props.show ? "block" : "none";
    
    
    return {display: display, color: color}
  }
  render(){
    if(!Config.admin){
      return null
    }
    
    return <div className="close-room" onClick={this.props.onLeave}>
      <i style={this.getStyles()} className="fa fa-close" />
    </div>
  }
}

Image.defaultProps = {
  show: null,
  onLeave: () => {}
};
