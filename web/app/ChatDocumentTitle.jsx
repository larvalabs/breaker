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
    var i, link_tag ;
    for (i = 0, link_tag = document.getElementsByTagName("link"); i < link_tag.length ; i++ ) {
      if ((link_tag[i].rel.indexOf("alternate") != -1) && link_tag[i].title) {
        link_tag[i].disabled = link_tag[i].title != roomName;
      }
    }
  }
  render() {
    return <DocumentTitle title={this.getTitle(this.props)}>
      {this.props.children}
    </DocumentTitle>
  }
}

function mapStateToProps(state) {
  return {
    unreadCount: state.getIn(['unreadCounts', state.get('currentRoom')]),
    roomName: state.get('currentRoom')
  }
}

export default connect(mapStateToProps)(ChatDocumentTitle)
