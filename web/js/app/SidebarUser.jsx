import React from 'react';

// TODO: No idea what this is on about
export default React.createClass({
  render: function() {
    return <div className="clearfix hidden-xs text-center hide" id="aside-user">
      <div className="dropdown wrapper">
        <a href="app.page.profile">
                  <span className="thumb-lg w-auto-folded avatar m-t-sm">
                    <img src="/public/img/user-anon.png" className="img-full" alt="..." />
                  </span>
        </a>
        <a href="#" data-toggle="dropdown" className="dropdown-toggle hidden-folded">
                  <span className="clear">
                    <span className="block m-t-sm">
                      <strong className="font-bold text-lt">John.Smith</strong>
                      <b className="caret" />
                    </span>
                    <span className="text-muted text-xs block">Art Director</span>
                  </span>
        </a>
        <ul className="dropdown-menu animated fadeInRight w hidden-folded">
          <li className="wrapper b-b m-b-sm bg-info m-t-n-xs">
            <span className="arrow top hidden-folded arrow-info"></span>
            <div>
              <p>300mb of 500mb used</p>
            </div>
            <div className="progress progress-xs m-b-none dker">
              <div className="progress-bar bg-white" data-toggle="tooltip" data-original-title="50%"
                   style={{width: "50%"}}></div>
            </div>
          </li>
          <li>
            <a href>Settings</a>
          </li>
          <li>
            <a href="page_profile.html">Profile</a>
          </li>
          <li>
            <a href>
              <span className="badge bg-danger pull-right">3</span>
              Notifications
            </a>
          </li>
          <li className="divider"></li>
          <li>
            <a href="page_signin.html">Logout</a>
          </li>
        </ul>
      </div>
      <div className="line dk hidden-folded"></div>
    </div>
  }
})
