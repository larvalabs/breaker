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

    if(!this.props.hasMore){
      return <span className="text-muted">This is the beginning of /r/{this.props.currentRoom}</span>
    }

    return <a className="more" onClick={this.props.handleMoreMessages}>More</a>
  }
  render(){
    if(this.props.messageCount < 20){
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
  hasMore: true,
  handleMoreMessages: () => {}
};

function mapStateToProps(state) {
  let roomMessages = state.getIn(['roomMessages', state.get('currentRoom')], Immutable.List());
  let firstMessage = state.getIn(['messages', roomMessages.first()]);
  let hasMore = firstMessage.get('type', '') !== 'first_sentinel';
  let messageCount = roomMessages.size;
  return {
    loading: state.getIn(['ui', 'moreMessagesLoading']),
    currentRoom: state.get('currentRoom'),
    hasMore,
    messageCount
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
