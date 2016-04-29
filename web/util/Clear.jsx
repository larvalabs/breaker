import React, {Component} from 'react'

export default class Clear extends Component {
  render(){
    return <div className="clear">
      {this.props.children}
    </div>
  }
}

