import React from "react";
import Header from './Header.jsx'
import Sidebar from './Sidebar.jsx'
import Main from './Main.jsx'

export default React.createClass({
  render: function() {
    return (
        <div className="app app-header-fixed app-aside-fixed">

          <Header user={User}/>
          <Sidebar activeRooms={ActiveRooms}/>
          <Main />
        </div>
    );
  }
});
