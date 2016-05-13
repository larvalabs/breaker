import React, {Component} from 'react'
import Immutable from 'immutable'
import TitleCollapsible from './TitleCollapsible'

export default class Image extends Component {
  constructor(props){
    super(props);
  }
  render(){
    return <div className="link-info">
      <TitleCollapsible title={this.props.linkInfo.get('title')} url={this.props.linkInfo.get('url')}
                        size={this.props.linkInfo.get('imageSize')} uuid={this.props.linkInfo.get('uuid')}>
        <img src={this.props.linkInfo.get('imageUrl')} className="image-preview"/>
      </TitleCollapsible>
    </div>;
  }
}

Image.defaultProps = {
  linkInfo: Immutable.Map()
};
