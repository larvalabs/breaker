import React, {Component} from 'react'
import Immutable from 'immutable'
import TitleCollapsible from './TitleCollapsible'
import {toggleCollapseLink} from '../redux/actions/chat-actions'
import { connect } from 'react-redux'


class VideoYouTube extends Component {
  constructor(props){
    super(props);
    this.state = {show: false};
    this.handleClick = this.handleClick.bind(this);
    this.handleToggleCollapse = this.handleToggleCollapse.bind(this);
  }
  handleClick(){
    this.setState({show:true});
  }
  handleToggleCollapse(){
    this.props.dispatch(toggleCollapseLink(this.props.linkInfo.get('uuid')));
  }
  renderVideo(){
    return <div data-url={this.props.linkInfo.get('url')}>
      <iframe width="400" oldwidth="400" height="300" oldheight="225"
              src={`${this.props.linkInfo.get('videoUrl')}?feature=oembed&amp;autoplay=1&amp;iv_load_policy=3`}
              frameborder="0" allowfullscreen=""></iframe>
    </div>
  }
  renderVideoThumbnail(){
    return <div className="video-thumbnail">
      <div className="video-buttons">
        <div className="video-button button-left">
          <i className="fa fa-play-circle" onClick={this.handleClick}/>
        </div>
        <div className="video-button button-right">
          <a href={this.props.linkInfo.get('url')} target="_blank">
            <i className="fa fa-external-link-square" />
          </a>
        </div>
      </div>
      <img className="" style={{width: "400px", height: "300px"}} src={this.props.linkInfo.get('imageUrl')}/>
    </div>
  }
  renderVideoPlayer(){
    if(this.state.show){
      return this.renderVideo();
    }

    return this.renderVideoThumbnail();
  }
  render(){
    let collapsed = this.props.collapsedLinks.contains(this.props.linkInfo.get('uuid'));

    return <div classNameName="link-info">
      <TitleCollapsible title={this.props.linkInfo.get('title')} url={this.props.linkInfo.get('url')}
                        collapsed={collapsed} onToggleCollapse={this.handleToggleCollapse}
                        size={this.props.linkInfo.get('imageSize')}>
        <div className="video">
          {this.renderVideoPlayer()}
        </div>
      </TitleCollapsible>
    </div>;
  }
}

VideoYouTube.defaultProps = {
  linkInfo: Immutable.Map()
};

function mapStateToProps(state) {
  return {
    collapsedLinks: state.getIn(['ui', 'collapsedLinks'], Immutable.Set())
  }
}


export default connect(mapStateToProps)(VideoYouTube)
