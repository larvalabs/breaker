import React, {Component} from 'react';
import { connect } from 'react-redux';
import Immutable from 'immutable';

import * as chatActions from '../redux/actions/chat-actions';

export class SidebarRoomHeader extends Component {
  constructor() {
    super();
    this.toggleRoomInput = this.toggleRoomInput.bind(this);
    this.mouseOver = this.mouseOver.bind(this);
    this.mouseOut = this.mouseOut.bind(this);
    this.handleChange = this.handleChange.bind(this);
    this.handlePressedEnter = this.handlePressedEnter.bind(this);

    this.state = {
      value: "",
      hover: false,
      isInputVisible: false
    };
  }

  toggleRoomInput() {
    this.setState({isInputVisible: !this.state.isInputVisible});
  }

  mouseOver() {
    this.setState({hover: true});
  }

  mouseOut() {
    this.setState({hover: false});
  }

  handleChange(event) {
    this.setState({value: event.target.value});
  }

  handlePressedEnter(event) {
    if (event.key === 'Enter') {
      const url = window.location.origin+"/r/"+this.state.value.trim();
      window.location.href = url;
    }
  }

  getRoomInput() {

    const groupStyles = {margin: '5px 0'};

    return (
      <div className='input-group' style={groupStyles}>
        <div className='input-group-addon'>#</div>
        <input type='text' className='form-control input-sm' placeholder='subreddit'
               value={this.state.value}
               onChange={this.handleChange}
               onKeyPress={this.handlePressedEnter}/>
      </div>
    );
  }

  render() {

    const hovered = (this.state.hover) ? {cursor: "pointer"} : {};
    const addBtnStyle = Object.assign({float: 'right', fontSize: '1.1em'},hovered);
    const { styles } = this.props;
    const iconClasses = (this.state.isInputVisible) ? 'fa fa-close' : 'fa fa-plus';

    return (
      <div>
        <span style={styles}>Your Rooms</span>
        <span>
          <i style={addBtnStyle} className={iconClasses}
                 onClick={this.toggleRoomInput}
                 onMouseOver={this.mouseOver}
                 onmouseout={this.mouseOut}></i>
        </span>
        {(this.state.isInputVisible) ? this.getRoomInput() : null}
      </div>
    );
  }
}
