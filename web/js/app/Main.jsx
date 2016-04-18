import React from 'react';
import ChatBox from './ChatBox.jsx'
import UserListBox from './UserListBox.jsx'

export default React.createClass({
  render: function() {
    return <div id="content" className="app-content" role="main">
      <div className="app-content-body app-content-full h-full">


        <div className="hbox bg-light ">
          <ChatBox />
          <UserListBox />
        </div>
      </div>
    </div>
  }
})
