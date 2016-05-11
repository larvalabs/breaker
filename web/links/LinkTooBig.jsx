import React, {Component} from 'react'
import Immutable from 'immutable'
import formatBytes from '../util/formatters'

export default class Image extends Component {
  constructor(props){
    super(props);
  }
  render(){
    let displaySize = formatBytes(this.props.size, 0);
    return <div className="link-info">
      <i className="link-too-big">Link not expanded because <strong>{displaySize}</strong> is too big. <a href={this.props.url} target="_blank">Open it in a new tab.</a></i>
    </div>
  }
}

Image.defaultProps = {
  title: null,
  url: null,
};
