import React, {Component} from 'react'
import ChatMessageHeader from './ChatMessageHeader'
import ChatShortMessage from './ChatShortMessage'

export default class ChatThread extends Component {
  renderThreadMessage(props, message, previous){

    return <div key={message.get('uuid')}>
      <ChatMessageHeader message={message}
                         previous={previous}
                         user={props.users.get(message.get('username'))}
                         roomName={props.roomName} />

      <ChatShortMessage message={message}
                        user={props.users.get(message.get('username'))}
                        roomName={props.roomName}/>
    </div>

  }

  renderThread(props) {
    let filteredMessages = props.messages.filter((message) => message);

    return <ul id="thread" className="list-group list-group-lg no-radius m-b-none m-t-n-xxs">
      {
        filteredMessages.map((message, index) => {
          let previous = null;
          if(index > 0){
            previous = filteredMessages.get(index-1)
          }
          return this.renderThreadMessage(this.props, message, previous)
        })
      }
    </ul>
  }
  render(){
    return <div className="row-row">
      <div id="thread_scrollparent" className="cell">
        <div className="cell-inner">

          {this.renderThread(this.props)}

          <div id="bottom-spacer" className="padder-v-sm bg-white b-l-3x b-l-white"></div>
        </div>
      </div>
    </div>
  }
}

