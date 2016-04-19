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
  render() {
    return <DocumentTitle title={this.getTitle(this.props)}>
      {this.props.children}
    </DocumentTitle>
  }
}

function mapStateToProps(state) {
  return {
    unreadCount: state.get('unreadCounts').reduce((total, value, key) => {
      return key == "__HAS_FOCUS__" ? total : total += value;
    }, 0)
  }
}

export default connect(mapStateToProps)(ChatDocumentTitle)
