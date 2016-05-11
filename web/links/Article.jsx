import React, {Component} from 'react'
import Immutable from 'immutable'


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
        <h5 className="title">
          <a href={this.props.linkInfo.get('url')} target="_blank">{this.props.linkInfo.get('title')}</a>
        </h5>
        <p className="description">{this.props.linkInfo.get('description')}</p>
      </div>

    </div>
  }
}

Article.defaultProps = {
  linkInfo: Immutable.Map()
};
