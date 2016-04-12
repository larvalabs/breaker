import React from 'react';

export default React.createClass({
  render: function() {
    return <div id="content" className="app-content" role="main">
      <div className="app-content-body app-content-full h-full">


        <div className="hbox bg-light " ng-init="
                  app.settings.asideFolded = true;
                  app.settings.asideFixed = true;
                  app.settings.asideDock = false;
                  app.settings.container = false;
                  app.hideAside = false
                  ">
          <div id="centercol" className="col">

            <div id="threadparent" className="vbox">
              <div className="row-row">
                <div id="thread_scrollparent" className="cell">
                  <div className="cell-inner">

                    <ul id="thread" className="list-group list-group-lg no-radius m-b-none m-t-n-xxs">
                    </ul>

                    <div id="bottom-spacer" className="padder-v-sm bg-white b-l-3x b-l-white"></div>
                  </div>
                </div>
              </div>

              <div className="padder padder-v b-t b-light text-center">
                            <textarea type="text" className="form-control input-message mention"
                                      placeholder="Type a message to ${roomName}..." />
              </div>

            </div>
          </div>

          <div id="rightcol" className="col w-md lter b-l hidden-sm hidden-xs">
            <div id="userlistparent" className="vbox">
              <div className="row-row">
                <div className="cell scrollable hover">
                  <div className="cell-inner">
                    <div role="tabpanel" className="tab-pane active" id="tab-1">
                      <div id='modparent' className="wrapper-md m-b-n-md">
                        <div className="m-b-sm text-md">Mods</div>
                        <ul id='modlist' className="list-group no-bg no-borders pull-in m-b-sm">
                        </ul>
                      </div>
                      <div id='onlineparent' className="wrapper-md m-b-n-md">
                        <div className="m-b-sm text-md">Here Now</div>
                        <ul id='onlinelist' className="list-group no-bg no-borders pull-in m-b-sm">
                        </ul>
                      </div>
                      <div className="wrapper-md">
                        <div className="m-b-sm text-md">All Users</div>
                        <ul id='userlist' className="list-group no-bg no-borders pull-in m-b-sm">
                        </ul>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  }
})
