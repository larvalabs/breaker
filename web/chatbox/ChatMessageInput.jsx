import React, {Component} from 'react';
import  Autosuggest from 'react-autosuggest';
import { connect } from 'react-redux';
import {sendNewMessage} from '../redux/actions/chat-actions'
import Immutable from 'immutable'

const theme = {
  "suggestionsContainer" : "suggestionsContainer",
  "input": "form-control input-message",
  "suggestion": "suggestion",
  "suggestionFocused": "suggestionFocused"
};

// HERE BE DRAGONS

class ChatMessageInput extends Component {
  constructor(props) {
    super(props);
    this.handleKeyPress = this.handleKeyPress.bind(this);
    this.onChange = this.onChange.bind(this);
    this.onSuggestionsUpdateRequested = this.onSuggestionsUpdateRequested.bind(this);
    this.onSuggestionSelected = this.onSuggestionSelected.bind(this);
    this.getSuggestionValue = this.getSuggestionValue.bind(this);
    this.getSuggestions = this.getSuggestions.bind(this);
    this.state = {
      value: '',
      suggestions: this.getSuggestions('')
    };
  }
  
  onChange(event, { newValue, method }) {
    return this.setState({
      value: newValue
    })
  }

  handleKeyPress(event) {
    if(event.key == 'Enter' && !event.isSuggestionSelected){
      event.preventDefault();
      this.props.dispatch(sendNewMessage({
        message: event.target.value,
        roomName: this.props.roomName
      }));
      this.setState({
        value: ""
      });
      this.props.onMessageInput();
    }
  }

  onSuggestionsUpdateRequested({ value, reason }) {
    this.setState({
      suggestions: this.getSuggestions(value)
    });
  }

  onSuggestionSelected(event, { suggestion, suggestionValue, sectionIndex, method }){
    if(method=='enter') {
      // This prevents the handleKeyPress from submitting text
      //  when selecting an autocomplete
      event.persist();
      event.isSuggestionSelected = true;
    }
  }

  getSuggestionValue(suggestion) {
    // When a suggestion is selected, we want to fill in
    // the suggestion in the rest of the input text.
    // Note: We assume the suggestion is always at the end.
    const lastInputTokens = this.state.value.trim().split(" ");
    const lastToken = lastInputTokens.pop();
    const lastTokenIsMention = lastToken.length !== 0 && lastToken[0] == "@";

    if(lastTokenIsMention) {
      let inputWithoutLastToken = lastInputTokens.join(" ");
      return (inputWithoutLastToken + " @").trim() + suggestion + " "
    }
    return suggestion;
  }

  getSuggestions(value) {
    const lastInputToken = value.toLowerCase().split(" ").pop();
    const userIsAttemptingMention = lastInputToken[0] === "@" && lastInputToken.length > 1;
    if(!userIsAttemptingMention){
      return []
    }

    const queryWithoutAt = lastInputToken.slice(1, lastInputToken.length);

    return this.props.members.filter(member => {
      return member.toLowerCase().slice(0, lastInputToken.length - 1) === queryWithoutAt;
    }).toJS();
  }

  renderSuggestion(suggestion) {
    return (
        <span>{suggestion}</span>
    );
  }
  renderPlaceholder(props){
    if(!props.connected){
      return "Disconnected from server"
    }

    return `Type a message to ${props.roomName}`
  }
  render() {
      const { value, suggestions } = this.state;
      const inputProps = {
        placeholder: this.renderPlaceholder(this.props),
        value,
        onChange: this.onChange,
        onKeyDown: this.handleKeyPress,
        disabled: !this.props.connected
      };

      return <Autosuggest suggestions={suggestions}
                   onSuggestionsUpdateRequested={this.onSuggestionsUpdateRequested}
                   getSuggestionValue={this.getSuggestionValue}
                   onSuggestionSelected={this.onSuggestionSelected}
                   renderSuggestion={this.renderSuggestion}
                   inputProps={inputProps}
                   theme={theme}/>;
  }
}

ChatMessageInput.defaultProps = {
  roomName: null
};

function mapStateToProps(state) {
  let roomName = state.getIn(['initial', 'roomName']);
  let membersMap = state.getIn(['members', roomName], Immutable.Map());
  let members = membersMap.reduce((a, b) => a.union(b), Immutable.OrderedSet()).toList();
  let connected = state.getIn(['ui', 'connected']);

  return {
    members,
    roomName,
    connected
  }
}

export default connect(mapStateToProps)(ChatMessageInput)
