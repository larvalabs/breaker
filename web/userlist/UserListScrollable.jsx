import React, { Component } from 'react';


export default class UserListScrollable extends Component {
  render() {
    return (
      <div className="col w-md lter b-l hidden-sm hidden-xs">
        <div className="vbox">
          <div className="row-row">
            <div className="cell scrollable hover">
              <div className="cell-inner">
                <div role="tabpanel" className="tab-pane active" id="tab-1">
                  {this.props.children}
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    );
  }
}
