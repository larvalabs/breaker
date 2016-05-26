import React, { Component } from 'react';
import Immutable from 'immutable';

import VideoHTML5 from './VideoHTML5';


export default class Video extends Component {
  render() {
    const site = this.props.linkInfo.get('site', '').toLocaleLowerCase();
    if (site === 'youtube') {
      return <VideoYouTube linkInfo={this.props.linkInfo} />;
    } else if (site) {
      return <VideoHTML5 linkInfo={this.props.linkInfo} />;
    }

    return null;
  }
}

Video.defaultProps = {
  linkInfo: Immutable.Map()
};
