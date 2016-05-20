import React, {Component} from 'react'
import Immutable from 'immutable'
import {connect} from 'react-redux'
import {toggleSettings} from '../redux/actions/menu-actions'

class MobileSettingsButton extends Component {
  render(){
    let classes = "pull-right visible-xs";
    if(this.props.settings_open){
      classes += " active";
    }
    let iconColor = this.props.room.getIn(['styles', 'sidebarTextColor'], "#EAEBED");
    let iconBGColor = this.props.room.getIn(['styles', 'sidebarBackgroundColor'], "#3a3f51");

    return  <button className={classes}
                    ui-toggle-className="show"
                    target=".navbar-collapse"
                    onClick={this.props.toggleSettings}
                    style={{backgroundColor: iconBGColor}}>

      <i className="glyphicon glyphicon-cog" style={{color: iconColor}}/>
    </button>
  }
}

MobileSettingsButton.defaultProps = {
  room: Immutable.Map(),
  settings_open: false
};

function mapDispatchToProps(dispatch){
  return {
    toggleSettings(){
      return dispatch(toggleSettings());
    }
  }
}

export default connect(null, mapDispatchToProps)(MobileSettingsButton);
