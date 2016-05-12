import React, {Component} from 'react'
import Immutable from 'immutable'
import Article from './Article'
import Image from './Image'
import Video from './Video'
import LinkTooBig from './LinkTooBig'


export default class LinkInfo extends Component {
  constructor(props){
    super(props);
  }
  render(){
    if(!this.props.linkInfo){
      return null
    }
    
    if(this.props.linkInfo.get('imageSize', 0) > 2000000){
      return <LinkTooBig size={this.props.linkInfo.get('imageSize')} url={this.props.linkInfo.get('url')} />
    }

    let type = this.props.linkInfo.get('breakerType');
    if(type === 'link'){
      return <Article linkInfo={this.props.linkInfo}/>
    } else if (type === 'image'){
      return <Image linkInfo={this.props.linkInfo}/>
    } else if (type === 'video') {
      return <Video linkInfo={this.props.linkInfo} />;
    } else {
      return null;
    }
  }
}

LinkInfo.defaultProps = {
  linkInfo: Immutable.Map(),
  uuid: null
};
