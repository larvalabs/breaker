import React, { Component, PropTypes } from 'react'
import { connect } from 'react-redux'
import DocumentTitle from 'react-document-title'

class ChatDocumentTitle extends Component {
  getTitle(props){
    if(props.unreadCount && props.unreadCount > 0){
      return `(${props.unreadCount}) breaker`
    }

    return `breaker`
  }
  componentDidMount() {
    this.changeSubredditStylesheet(this.props.roomName)
  }
  componentWillReceiveProps(nextProps) {
    this.changeSubredditStylesheet(nextProps.roomName)
  }
  changeSubredditStylesheet(roomName){
    document.getElementById("subreddit-style")
            .setAttribute("href", `https://www.reddit.com/r/${roomName}/stylesheet.css`);
  }
  render() {
    return <DocumentTitle title={this.getTitle(this.props)}>
      {this.props.children}
    </DocumentTitle>
  }
}

function mapStateToProps(state) {
  return {
    unreadCount: state.getIn(['unreadCounts', state.getIn(['initial', 'roomName'])]),
    roomName: state.getIn(['initial', 'roomName'])
  }
}

export default connect(mapStateToProps)(ChatDocumentTitle)
