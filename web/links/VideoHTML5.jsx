import React, {Component} from 'react'
import Immutable from 'immutable'
import TitleCollapsible from './TitleCollapsible'
import ReactDOM from 'react-dom'


export default class VideoYouTube extends Component {
  constructor(props){
    super(props);
    this.state = {playVideo: false};
    this.handleClick = this.handleClick.bind(this);
    this.calculateDimensions = this.calculateDimensions.bind(this);
    this.calculateDimensionStyles = this.calculateDimensionStyles.bind(this);
  }
  calculateDimensions(){
    let height = this.props.linkInfo.get('videoHeight');
    let width = this.props.linkInfo.get('videoWidth');

    if (height > width && height > 400){
      width = (400 / height) * width;
      height = 400;
    } else if(width > 400){
      height = (400 / width) * height;
      width = 400
    }

    return {
      height: height,
      width: width
    }
  }
  calculateDimensionStyles(){
    const dimensions = this.calculateDimensions();
    return {
      height: dimensions.height + "px",
      width: dimensions.width + "px"
    }
  }
  handleClick(){
    this.setState({playVideo:true});
  }
  renderVideo(){
    const dimensions = this.calculateDimensions();
    return <video controls autoPlay loop
                  poster={this.props.linkInfo.get('imageUrl')} height={dimensions.height} width={dimensions.width}>
      <source src={this.props.linkInfo.get('videoUrl')} type="video/mp4" />
      <p>
        Your browser doesn't support HTML5 video.
        <a href={this.props.linkInfo.get('imageUrl')}>Download</a> the video instead.
      </p>
    </video>
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
      <img className="" style={this.calculateDimensionStyles()}
           src={this.props.linkInfo.get('imageUrl')}/>
    </div>
  }
  renderVideoPlayer(){
    if(this.state.playVideo){
      return this.renderVideo();
    }

    return this.renderVideoThumbnail();
  }
  render(){
    return <div className="link-info">
      <h5 className="site"><a href={this.props.linkInfo.get('url')}>{this.props.linkInfo.get('site')}</a></h5>
      <TitleCollapsible title={this.props.linkInfo.get('title')} url={this.props.linkInfo.get('url')}
                        uuid={this.props.linkInfo.get('uuid')}>
        <div className="video html5" style={this.calculateDimensionStyles()}>
          {this.renderVideoPlayer()}
        </div>
      </TitleCollapsible>
    </div>;
  }
}

VideoYouTube.defaultProps = {
  linkInfo: Immutable.Map()
};
