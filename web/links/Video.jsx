import React, {Component} from 'react'
import Immutable from 'immutable'
import VideoYouTube from './VideoYouTube'

export default class Article extends Component {
  constructor(props){
    super(props);
  }
  render(){
    if(this.props.linkInfo.get('site', '').toLocaleLowerCase() === 'youtube'){
      return <VideoYouTube linkInfo={this.props.linkInfo} />
    }
    
    return null;
  }
}

Article.defaultProps = {
  linkInfo: Immutable.Map()
};
