import React, {Component} from 'react'
import Immutable from 'immutable'
import Title from './Title'

export default class Article extends Component {
  constructor(props){
    super(props);
  }
  renderImage(linkInfo){
    let imageUrl = linkInfo.get('imageUrl');
    if(!imageUrl){
      return null;
    }
    return <img style={{float: "right"}} src={imageUrl} height="50px"/>
  }
  render(){
    return <div className="link-info">
      {this.renderImage(this.props.linkInfo)}
      <div>
        <Title title={this.props.linkInfo.get('title')} url={this.props.linkInfo.get('url')} />
        <p className="description">{this.props.linkInfo.get('description')}</p>
      </div>

    </div>
  }
}

Article.defaultProps = {
  linkInfo: Immutable.Map()
};
