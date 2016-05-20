import React, {Component} from 'react'
import Immutable from 'immutable'
import {connect} from 'react-redux'
import {toggleSidebar} from '../redux/actions/menu-actions'

class MobileMenuButton extends Component {
  render(){
    let classes = "pull-right visible-xs";
    let active = this.props.sidebar_open ? " active" : "";
    let iconColor = this.props.room.getIn(['styles', 'sidebarTextColor'], "#EAEBED");
    let iconBGColor = this.props.room.getIn(['styles', 'sidebarBackgroundColor'], "#3a3f51");
    return <button className={classes + active} ui-toggle-className="off-screen"
                   target=".app-aside" ui-scroll="app" onClick={this.props.toggleSidebar}>
      <i className="glyphicon glyphicon-align-justify" style={{color: iconColor, background: iconBGColor}}/>
    </button>
  }
}

MobileMenuButton.defaultProps = {
  room: Immutable.Map(),
  sidebar_open: false
};

function mapDispatchToProps(dispatch){
  return {
    toggleSidebar(){
      return dispatch(toggleSidebar());
    }
  }
}

export default connect(null, mapDispatchToProps)(MobileMenuButton);
