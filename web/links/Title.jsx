import React, {Component} from 'react'
import Immutable from 'immutable'


export default class Image extends Component {
  constructor(props){
    super(props);
  }
  render(){
    return <h5 className="title">
        <a href={this.props.url} target="_blank">{title}</a>
      </h5>
  }
}

Image.defaultProps = {
  title: null,
  url: null,
};
