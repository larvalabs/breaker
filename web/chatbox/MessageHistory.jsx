import React, {Component} from 'react'
import Immutable from 'immutable'
import { connect } from 'react-redux'
import * as chatActions from '../redux/actions/chat-actions'

export default class MessageHistory extends Component {
  constructor(props){
    super(props);
  }
  renderTitle(){
    if(this.props.loading){
      return <span className="text-muted">Getting history...</span>
    }

    return <a className="more" onClick={this.props.handleMoreMessages}>More</a>
  }
  render(){
    if(this.props.message_count < 20){
      return null;
    }
    return <li className="message-fetch-message">
      {this.renderTitle()}
      <div className="divider"></div>
    </li>
  }
}

MessageHistory.defaultProps = {
  message_count: 0,
  loading: false,
  handleMoreMessages: () => {}
};

function mapStateToProps(state) {

  return {
    loading: state.getIn(['ui', 'moreMessagesLoading']),
    currentRoom: state.get('currentRoom'),
    message_count: state.getIn(['roomMessages', state.get('currentRoom')], Immutable.List()).size
  }
}

function mapDispatchToProps(dispatch) {
  return {
    handleMoreMessages(){
      dispatch(chatActions.handleMoreMessages())
    }
  }
}

export default connect(mapStateToProps, mapDispatchToProps)(MessageHistory)
