import React, { Component } from 'react';

import formatBytes from '../util/formatters';


export default class LinkTooBig extends Component {
  render() {
    const displaySize = formatBytes(this.props.size, 0);
    return (
      <div className="link-info">
        <i className="link-too-big">
          Link not expanded because <strong>{displaySize}</strong> is too big.&nbsp;
          <a href={this.props.url} target="_blank">Open it in a new tab.</a>
        </i>
      </div>
    );
  }
}

LinkTooBig.defaultProps = {
  title: null,
  url: null
};
