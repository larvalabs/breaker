import React, {Component} from 'react'
import formatBytes from '../util/formatters'

export default class TitleCollapsible extends Component {
  constructor(props){
    super(props);
    this.handleClick = this.handleClick.bind(this);
  }
  handleClick(){
    this.props.onToggleCollapse();
  }
  renderCollapse(){
    let classes = this.props.collapsed ? "fa-caret-right" : "fa-caret-down";
    return <i className={`fa ${classes} link-collapse`} onClick={this.handleClick}></i>
  }
  renderSize(){
    if(!this.props.size){
      return null;
    }

    return <span> ({formatBytes(this.props.size, 0)})</span>
  }
  render(){
    let title = this.props.title;
    if(!title){
      title = this.props.url;
    }

    if(!title){
      return null
    }

    return <h5 className="title">
        <a href={this.props.url} target="_blank">{title}</a>{this.renderSize()}
        {this.renderCollapse()}
      </h5>
  }
}

TitleCollapsible.defaultProps = {
  title: null,
  url: null,
  collapsed: false,
  onToggleCollapse: () => {}
};
