import React, {Component} from 'react'
import Immutable from 'immutable'
import Article from './Article'
import Image from './Image'


export default class LinkInfo extends Component {
  constructor(props){
    super(props);
  }
  render(){
    if(this.props.linkInfo && this.props.linkInfo.get('description')){
      return <Article linkInfo={this.props.linkInfo} />
    } else if (this.props.linkInfo.get('imageUrl')){
      return <Image linkInfo={this.props.linkInfo} />
    } else {
      return null;
    }
  }
}

LinkInfo.defaultProps = {
  linkInfo: Immutable.Map()
};
