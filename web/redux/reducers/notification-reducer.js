import Immutable from 'immutable';

import * as notifyTypes from '../constants/notification-constants';


const initialState = Immutable.fromJS({
  ignore: true,
  title: ''
});

export default function notification(state = initialState, action) {
  switch (action.type) {
    case (notifyTypes.NOTIFY_PERMISSION_GREANTED): {
      return state.set('ignore', false);
    }
    case (notifyTypes.NOTIFY_PERMISSION_DENIED): {
      return state.set('ignore', true);
    }
    case (notifyTypes.NOTIFY_NOT_SUPPORTED): {
      return state.set('ignore', true);
    }
    case (notifyTypes.NOTIFY_ON_CLICK): {
      return state;
    }
    case (notifyTypes.NOTIFY_ON_CLOSE): {
      return state;
    }
    case (notifyTypes.NOTIFY_ON_ERROR): {
      return state;
    }
    case (notifyTypes.NOTIFY_ON_SHOW): {
      return state;
    }
    case (notifyTypes.NOTIFY_SEND): {
      return Immutable.fromJS({
        ignore: state.get('ignore'),
        title: action.title,
        options: {
          tag: action.tag,
          body: action.body,
          icon: 'http://georgeosddev.github.io/react-web-notification/example/Notifications_button_24.png',
          lang: 'en',
          dir: 'ltr',
          data: {
            roomName: action.roomName
          }
        }
      });
    }

    default: {
      return state;
    }
  }
}
