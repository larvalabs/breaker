import React, { Component } from 'react';
import { connect } from 'react-redux';
import Immutable from 'immutable';

import Autosuggest from 'react-autosuggest';
import { sendNewMessage, resetChatInputFocus, setChatInputFocus } from '../redux/actions/chat-actions';
import { handleCloseAllMenus } from '../redux/actions/menu-actions';

import { getConnected, getSetInputFocus } from '../redux/selectors/ui-selectors';
import { getAllMembersForCurrentRoom } from '../redux/selectors/members-selectors';

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
    const { setInputFocusValue, inputFocusWasSet } = this.props;

    if (!setInputFocusValue) {
      return false;
    }
    this._input.focus();
    inputFocusWasSet();
  }

  onChange(event, { newValue }) {
    return this.setState({
      value: newValue
    });
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
    const lastTokenIsMention = lastToken.length !== 0 && lastToken[0] === '@';

    if (lastTokenIsMention) {
      const inputWithoutLastToken = lastInputTokens.join(' ');
      const inputWithAtTrimmed = `${inputWithoutLastToken} @`.trim();
      return `${inputWithAtTrimmed}${suggestion} `;
    }
    return suggestion;
  }

  getSuggestions(value) {
    const { members } = this.props;
    const lastInputToken = value.toLowerCase().split(' ').pop();
    const userIsAttemptingMention = lastInputToken[0] === '@' && lastInputToken.length > 1;
    if (!userIsAttemptingMention) {
      return [];
    }

    const queryWithoutAt = lastInputToken.slice(1, lastInputToken.length);
    return members.filter(member => {
      return member.toLowerCase().slice(0, lastInputToken.length - 1) === queryWithoutAt;
    }).toJS();
  }

  handleKeyPress(event) {
    const { roomName, onSendNewMessage, onMessageInput } = this.props;

    if (event.key === 'Enter' && !event.isSuggestionSelected) {
      event.preventDefault();
      onSendNewMessage(roomName, event.target.value);
      this.setState({
        value: ''
      });

      onMessageInput();
    }
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
    const { connected, roomName } = this.props;

    if (!connected) {
      return 'Disconnected from server';
    }

    return `Type a message to #${roomName}...`;
  }

  render() {
    const { value, suggestions } = this.state;
    const { connected, closeSidebar } = this.props;

    const inputProps = {
      placeholder: this.renderPlaceholder(),
      value,
      onChange: this.onChange,
      onKeyDown: this.handleKeyPress,
      onFocus: closeSidebar,
      disabled: !connected,
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
  roomName: '',
  members: Immutable.List(),
  connected: true,
  setInputFocusValue: false,
  closeSidebar: () => {},
  inputFocusWasSet: () => {},
  setChatInputFocus: () => {},
  onSendNewMessage: () => {}
};

function mapStateToProps(state) {
  return {
    members: getAllMembersForCurrentRoom(state),
    roomName: state.get('currentRoom'),
    connected: getConnected(state),
    setInputFocusValue: getSetInputFocus(state)
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
    onSendNewMessage(roomName, message) {
      dispatch(sendNewMessage({
        message,
        roomName
      }));
    }
  };
}

export default connect(mapStateToProps, mapDispatchToProps)(ChatMessageInput)
