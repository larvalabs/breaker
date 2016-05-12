import React, {Component} from 'react'
import Immutable from 'immutable'
import TitleCollapsible from './TitleCollapsible'

export default class VideoYouTube extends Component {
  constructor(props){
    super(props);
  }
  renderVideo(){
    return <div data-url={this.props.linkInfo.get('url')}>
      <iframe width="400" oldwidth="400" height="300" oldheight="225"
              src={`${this.props.linkInfo.get('url')}?feature=oembed&amp;autoplay=1&amp;iv_load_policy=3`}
              frameborder="0" allowfullscreen=""></iframe>
    </div>
  }
  render(){
    return <div className="link-info">
      <TitleCollapsible title={this.props.linkInfo.get('title')} url={this.props.linkInfo.get('url')}
                        collapsed={false} onToggleCollapse={this.handleToggleCollapse}
                        size={this.props.linkInfo.get('imageSize')}/>
      {this.renderVideo()}
    </div>;
  }
}

VideoYouTube.defaultProps = {
  linkInfo: Immutable.Map()
};
