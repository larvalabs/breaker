import React, { Component } from 'react';
import { connect } from 'react-redux';

import ReactDocumentTitle from 'react-document-title';
import { getTotalLastSeenTimes } from '../redux/selectors/last-seen-selectors'


class DocumentTitle extends Component {
  componentDidMount() {
    const { roomName } = this.props;

    this.getTitle = this.getTitle.bind(this);
    this.changeSubredditStylesheet(roomName);
  }

  componentWillReceiveProps(nextProps) {
    this.changeSubredditStylesheet(nextProps.roomName);
  }

  getTitle() {
    const { unreadCount } = this.props;

    if (unreadCount && unreadCount > 0) {
      return `(${unreadCount}) breaker`;
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
      <ReactDocumentTitle title={this.getTitle()}>
        {this.props.children}
      </ReactDocumentTitle>
    );
  }
}

DocumentTitle.defaultProps = {
  unreadCount: 0,
  roomName: ''
};

function mapStateToProps(state) {

  return {
    roomName: state.get('currentRoom'),
    unreadCount: getTotalLastSeenTimes(state)
  };
}

export default connect(mapStateToProps)(DocumentTitle);
