import React, { Component } from 'react';

import DocumentTitle from 'react-document-title';


export default class ChatDocumentTitle extends Component {
  componentDidMount() {
    this.changeSubredditStylesheet(this.props.roomName)
  }

  componentWillReceiveProps(nextProps) {
    this.changeSubredditStylesheet(nextProps.roomName)
  }

  getTitle(props) {
    if (props.unreadCount && props.unreadCount > 0) {
      return `(${props.unreadCount}) breaker`;
    }

    return `breaker`;
  }

  changeSubredditStylesheet(roomName) {
    let i;
    let linkTag;
    for (i = 0, linkTag = document.getElementsByTagName('link'); i < linkTag.length; i++) {
      if ((linkTag[i].rel.indexOf('alternate') !== -1) && linkTag[i].title) {
        linkTag[i].disabled = linkTag[i].title !== roomName;
      }
    }
  }
  render() {
    return (
      <DocumentTitle title={this.getTitle(this.props)}>
        {this.props.children}
      </DocumentTitle>
    );
  }
}
