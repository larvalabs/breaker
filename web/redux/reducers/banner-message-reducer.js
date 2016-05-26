import Immutable from 'immutable';

import * as socketTypes from '../constants/socket-constants';


export default function bannerMessage(state = Immutable.Map(), action) {
  switch (action.type) {
    case (socketTypes.SOCK_CLOSE): {
      return Immutable.Map({
        type: 'error',
        body: 'Disconnected from server, will retry...'
      });
    }
    case (socketTypes.SOCK_OPEN): {
      return Immutable.Map();
    }
    default: {
      return state;
    }
  }
}
