import React, {Component} from 'react'


export default class Image extends Component {
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
  render(){
    let title = this.props.title;
    if(!title){
      title = this.props.url;
    }

    if(!title){
      return null
    }

    return <h5 className="title">
        <a href={this.props.url} target="_blank">{title}</a>
        {this.renderCollapse()}
      </h5>
  }
}

Image.defaultProps = {
  title: null,
  url: null,
  collapsed: false,
  onToggleCollapse: () => {}
};
