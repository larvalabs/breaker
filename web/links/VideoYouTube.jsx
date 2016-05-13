import React, {Component} from 'react'
import Immutable from 'immutable'
import TitleCollapsible from './TitleCollapsible'


export default class VideoYouTube extends Component {
  constructor(props){
    super(props);
    this.state = {show: false};
    this.handleClick = this.handleClick.bind(this);
  }
  handleClick(){
    this.setState({show:true});
  }
  renderVideo(){
    return <div data-url={this.props.linkInfo.get('url')}>
      <iframe width="400" oldwidth="400" height="300" oldheight="225"
              src={`${this.props.linkInfo.get('videoUrl')}?feature=oembed&amp;autoplay=1&amp;iv_load_policy=3`}
              frameBorder="0" allowFullScreen=""></iframe>
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
    return <div classNameName="link-info">
      <TitleCollapsible title={this.props.linkInfo.get('title')} url={this.props.linkInfo.get('url')}
                        size={this.props.linkInfo.get('imageSize')} uuid={this.props.linkInfo.get('uuid')}>
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
