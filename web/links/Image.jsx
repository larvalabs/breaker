import React, {Component} from 'react'
import Immutable from 'immutable'


export default class Image extends Component {
  constructor(props){
    super(props);
  }
  render(){
    return <div className="link-info">
      <h5 className="title">
        <a href={this.props.linkInfo.get('url')} target="_blank">{this.props.linkInfo.get('title')}</a>
      </h5>
      <img src={this.props.linkInfo.get('imageUrl')} className="image-preview"/>
    </div>;
  }
}

Image.defaultProps = {
  linkInfo: Immutable.Map()
};
