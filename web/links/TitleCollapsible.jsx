import React, { Component } from 'react';
import { connect } from 'react-redux';
import Immutable from 'immutable';

import formatBytes from '../util/formatters';

import { toggleCollapseLink } from '../redux/actions/chat-actions';


export default class TitleCollapsible extends Component {
  constructor(props) {
    super(props);
    this.handleClick = this.handleClick.bind(this);
    this.handleToggleCollapse = this.handleToggleCollapse.bind(this);
  }

  handleClick() {
    this.props.onToggleCollapse();
  }

  handleToggleCollapse() {
    this.props.dispatch(toggleCollapseLink(this.props.uuid));
  }

  renderCollapse() {
    const classes = this.props.collapsed ? 'fa-caret-right' : 'fa-caret-down';
    return <i className={`fa ${classes} link-collapse`} onClick={this.handleToggleCollapse} />
  }

  renderSize() {
    if (!this.props.size) {
      return null;
    }

    return <span> ({formatBytes(this.props.size, 0)})</span>;
  }

  renderBody() {
    if (this.props.collapsed) {
      return null;
    }

    return this.props.children;
  }
  render() {
    let title = this.props.title;
    if (!title) {
      title = this.props.url;
    }

    if (!title) {
      return null;
    }

    return (
      <div>
        <h5 className="title">
        <a href={this.props.url} target="_blank">{title}</a>{this.renderSize()}
          {this.renderCollapse()}
        </h5>
        {this.renderBody()}
      </div>
    );
  }
}

TitleCollapsible.defaultProps = {
  title: null,
  url: null,
  collapsed: false,
  onToggleCollapse: () => {}
};

function mapStateToProps(state, ownProps) {
  return {
    collapsed: state.getIn(['ui', 'collapsedLinks'], Immutable.Map()).contains(ownProps.uuid)
  };
}

export default connect(mapStateToProps)(TitleCollapsible)
