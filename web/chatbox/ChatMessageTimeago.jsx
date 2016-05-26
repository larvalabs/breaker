import React, { Component } from 'react';
import TimeAgo from 'react-timeago';


export default class ChatMessageTimeago extends Component {
  render() {
    return (
      <div className="pull-right text-sm hidden-xs text-muted">
        <TimeAgo date={new Date(this.props.time).toISOString()} />
      </div>
    );
  }
}
