import React, {Component} from 'react';
import {connect} from 'react-redux'
import {handleCloseAllMenus} from '../redux/actions/menu-actions'

class MobileRoomOverlay extends Component {
  
  render() {
    let styles = {
      position:"absolute", 
      top: 0,
      left: 0,
      right: 0,
      bottom: "71px",
      zIndex: 1,
      display: this.props.show ? "block" : "none"
    };
    return <div style={styles} onClick={this.props.closeSidebar}></div>
  }
}

function mapStateToProps(state){

  return {
    show: state.getIn(['ui', 'sidebar_open']) || state.getIn(['ui', 'settings_open']),
  }
}

function mapDispatchToProps(dispatch){
  return {
    closeSidebar(){
      dispatch(handleCloseAllMenus())
    }
  }
}

export default connect(mapStateToProps, mapDispatchToProps)(MobileRoomOverlay)
