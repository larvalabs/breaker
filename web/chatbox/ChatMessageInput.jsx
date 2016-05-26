import React, { Component } from 'react';
import { connect } from 'react-redux';
import Immutable from 'immutable';

import Autosuggest from 'react-autosuggest';
import { sendNewMessage, resetChatInputFocus, setChatInputFocus } from '../redux/actions/chat-actions';
import { handleCloseAllMenus } from '../redux/actions/menu-actions';


const theme = {
  suggestionsContainer: 'suggestionsContainer',
  input: 'form-control input-message',
  suggestion: 'suggestion',
  suggestionFocused: 'suggestionFocused'
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
    this.handleRef = this.handleRef.bind(this);
    this.state = {
      value: '',
      suggestions: this.getSuggestions('')
    };
  }
  
  componentDidMount() {
    this.props.setChatInputFocus();
  }
  
  componentDidUpdate() {
    if (!this.props.setInputFocusValue) {
      return false;
    }
    this._input.focus();
    this.props.inputFocusWasSet();
  }

  onChange(event, { newValue }) {
    return this.setState({
      value: newValue
    });
  }

  handleKeyPress(event) {
    if (event.key === 'Enter' && !event.isSuggestionSelected) {
      event.preventDefault();
      this.props.sendNewMessage(this.props.roomName, event.target.value);
      this.setState({
        value: ''
      });

      this.props.onMessageInput();
    }
  }

  onSuggestionsUpdateRequested({ value }) {
    this.setState({
      suggestions: this.getSuggestions(value)
    });
  }

  onSuggestionSelected(event, { method }) {
    if (method === 'enter') {
      // This prevents the handleKeyPress from submitting text
      //  when selecting an autocomplete
      event.persist();
      event.isSuggestionSelected = true; // eslint-disable-line
    }
  }

  getSuggestionValue(suggestion) {
    // When a suggestion is selected, we want to fill in
    // the suggestion in the rest of the input text.
    // Note: We assume the suggestion is always at the end.
    const lastInputTokens = this.state.value.trim().split(' ');
    const lastToken = lastInputTokens.pop();
    const lastTokenIsMention = lastToken.length !== 0 && lastToken[0] == '@';

    if (lastTokenIsMention) {
      const inputWithoutLastToken = lastInputTokens.join(' ');
      const inputWithAtTrimmed = `${inputWithoutLastToken} @`.trim();
      return `${inputWithAtTrimmed}${suggestion} `;
    }
    return suggestion;
  }

  getSuggestions(value) {
    const lastInputToken = value.toLowerCase().split(' ').pop();
    const userIsAttemptingMention = lastInputToken[0] === '@' && lastInputToken.length > 1;
    if (!userIsAttemptingMention) {
      return [];
    }

    const queryWithoutAt = lastInputToken.slice(1, lastInputToken.length);

    return this.props.members.filter(member => {
      return member.toLowerCase().slice(0, lastInputToken.length - 1) === queryWithoutAt;
    }).toJS();
  }

  handleRef(ref) {
    this._input = ref;
  }

  renderSuggestion(suggestion) {
    return (
        <span>{suggestion}</span>
    );
  }

  renderPlaceholder(props) {
    if (!props.connected) {
      return 'Disconnected from server';
    }

    return `Type a message to #${props.roomName}...`;
  }

  render() {
    const { value, suggestions } = this.state;
    const inputProps = {
      placeholder: this.renderPlaceholder(this.props),
      value,
      onChange: this.onChange,
      onKeyDown: this.handleKeyPress,
      onFocus: this.props.closeSidebar,
      disabled: !this.props.connected,
      ref: this.handleRef
    };

    return (
      <Autosuggest suggestions={suggestions}
                   onSuggestionsUpdateRequested={this.onSuggestionsUpdateRequested}
                   getSuggestionValue={this.getSuggestionValue}
                   onSuggestionSelected={this.onSuggestionSelected}
                   renderSuggestion={this.renderSuggestion}
                   inputProps={inputProps}
                   theme={theme}
                   tabToSelect
                   selectFirstSuggestion
      />
    );
  }
}

ChatMessageInput.defaultProps = {
  roomName: null
};

function mapStateToProps(state) {
  const roomName = state.get('currentRoom');
  const membersMap = state.getIn(['members', roomName], Immutable.Map());
  const members = membersMap.reduce((a, b) => a.union(b), Immutable.OrderedSet()).toList();
  const connected = state.getIn(['ui', 'connected']);
  const setInputFocusValue = state.getIn(['ui', 'setInputFocus'], false);

  return {
    members,
    roomName,
    connected,
    setInputFocusValue
  };
}

function mapDispatchToProps(dispatch) {
  return {
    closeSidebar() {
      dispatch(handleCloseAllMenus());
    },
    inputFocusWasSet() {
      dispatch(resetChatInputFocus());
    },
    setChatInputFocus() {
      dispatch(setChatInputFocus());
    },
    sendNewMessage(roomName, message) {
      dispatch(sendNewMessage({
        message,
        roomName
      }));
    }
  };
}

export default connect(mapStateToProps, mapDispatchToProps)(ChatMessageInput)
