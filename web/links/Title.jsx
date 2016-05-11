import React, {Component} from 'react'
import Immutable from 'immutable'


export default class Image extends Component {
  constructor(props){
    super(props);
    this.handleClick = this.handleClick.bind(this);
    this.state = {
      atom: Immutable.Map({collapse: false})
    };
  }
  handleClick(){
    let current = this.state.atom.get('collapse');
    this.setState({atom: this.state.atom.set('collapse', !current)});
    this.props.onCollapse(!current);
  }
  renderCollapse(){
    let classes = this.state.atom.get('collapse') ? "fa-caret-right" : "fa-caret-down";
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
  onCollapse: () => {}
};
