import React, { Component } from 'react';
import Immutable from 'immutable';


export default class SidebarRoomSelect extends Component {
  constructor(props) {
    super(props);
    this.getStyles = this.getStyles.bind(this);
    this.onMouseEnter = this.onMouseEnter.bind(this);
    this.onMouseLeave = this.onMouseLeave.bind(this);

    this.state = {
      hover: false
    };
  }

  onMouseEnter() {
    this.setState({
      hover: true
    });
  }

  onMouseLeave() {
    this.setState({
      hover: false
    });
  }

  getStyles() {
    const { active, styles } = this.props;
    const { hover } = this.state;

    let backgroundColor = null;
    if (active) {
      backgroundColor = styles.get('sidebarRoomSelectedColor');
    } else if (hover) {
      backgroundColor = styles.get('sidebarRoomHoverColor');
    } else {
      return {};
    }

    return { backgroundColor };
  }

  render() {
    const { children } = this.props;

    return (
      <a className="roomselect" style={this.getStyles()}
         onMouseLeave={this.onMouseLeave} onMouseEnter={this.onMouseEnter}
      >
        {children}
      </a>
    );
  }
}

SidebarRoomSelect.defaultProps = {
  styles: Immutable.Map(),
  active: false
};
