import React, {Component} from 'react'
import Immutable from 'immutable'
import MobileMenuButton from './MobileMenuButton'
import MobileSettingsButton from './MobileSettingsButton'

export default class MobileMenuButtons extends Component {
  render(){
    return <div>
      
    </div>
  }
}

MobileMenuButtons.defaultProps = {
  room: Immutable.Map(),
  sidebar_open: false,
  settings_open: false
};
