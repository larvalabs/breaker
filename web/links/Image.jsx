import React, {Component} from 'react'
import Immutable from 'immutable'
import TitleCollapsible from './TitleCollapsible'
import { connect } from 'react-redux'
import {toggleCollapseLink} from '../redux/actions/chat-actions'

class Image extends Component {
  constructor(props){
    super(props);
    this.renderImage = this.renderImage.bind(this);
  }
  handleToggleCollapse(){
    this.props.dispatch(toggleCollapseLink(this.props.linkInfo.get('uuid')));
  }
  renderImage(collapsed){
    if(collapsed){
      return null;
    }

    return <img src={this.props.linkInfo.get('imageUrl')} className="image-preview"/>
  }
  render(){
    let collapsed = this.props.collapsedLinks.contains(this.props.linkInfo.get('uuid'));
    
    return <div className="link-info">
      <TitleCollapsible title={this.props.linkInfo.get('title')} url={this.props.linkInfo.get('url')}
                        collapsed={collapsed} onToggleCollapse={this.handleToggleCollapse} />
      {this.renderImage(collapsed)}
    </div>;
  }
}

Image.defaultProps = {
  linkInfo: Immutable.Map()
};

function mapStateToProps(state) {
  return {
    collapsedLinks: state.getIn(['ui', 'collapsedLinks'], Immutable.Set())
  }
}


export default connect(mapStateToProps)(Image)
