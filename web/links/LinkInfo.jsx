import React, {Component} from 'react'
import Immutable from 'immutable'
import Article from './Article'
import Image from './Image'


export default class LinkInfo extends Component {
  constructor(props){
    super(props);
  }
  render(){
    if(!this.props.linkInfo){
      return null
    }

    let type = this.props.linkInfo.get('type');
    if(type === 'link'){
      return <Article linkInfo={this.props.linkInfo} />
    } else if (type === 'image'){
      return <Image linkInfo={this.props.linkInfo} />
    } else {
      return null;
    }
  }
}

LinkInfo.defaultProps = {
  linkInfo: Immutable.Map()
};
