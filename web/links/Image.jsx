import React, {Component} from 'react'
import Immutable from 'immutable'
import Title from './Title'

export default class Image extends Component {
  constructor(props){
    super(props);
    this.onCollapse = this.onCollapse.bind(this);
    this.renderImage = this.renderImage.bind(this);
    this.state = {
      atom: Immutable.Map({collapse: false})
    }
  }
  onCollapse(value){
    this.setState({
      atom: this.state.atom.set('collapse', value)
    });
  }
  renderImage(){
    if(this.state.atom.get('collapse')){
      return null;
    }

    return <img src={this.props.linkInfo.get('imageUrl')} className="image-preview"/>
  }
  render(){
    return <div className="link-info">
      <Title title={this.props.linkInfo.get('title')}
             url={this.props.linkInfo.get('url')} onCollapse={this.onCollapse}/>
      {this.renderImage()}
    </div>;
  }
}

Image.defaultProps = {
  linkInfo: Immutable.Map()
};
