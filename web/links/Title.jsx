import React, { Component } from 'react';


export default class Title extends Component {
  constructor(props) {
    super(props);
  }
  render() {
    return (
      <h5 className="title">
        <a href={this.props.url} target="_blank">{this.props.title}</a>
      </h5>
    );
  }
}

Title.defaultProps = {
  title: null,
  url: null
};
