import React, {Component} from 'react'


export default class ChatBox extends Component {
  render() {
    return <div id="centercol" className="col">

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
  }
}
