import EventEmitter from 'events';

import PokeyActions from '../actions/PokeyActions'
import PokeyApi from '../api/PokeyApi';
import PokeyApiEvents from '../api/PokeyApiEvents';
import AppDispatcher from '../dispatcher/appDispatcher';

const EVENTS = {
  CHANGE: 'CHANGE',
  ERROR: 'ERROR'
};

var _currentUser = null;
var _currentRoom = null;

class PokeyStore extends EventEmitter {
  constructor() {
    super();

    // TODO Wire into dispatcher and into API
    AppDispatcher.register((action) => {
      switch (action.type) {
        case PokeyActions.AppStarted:
          console.log('app_started');
          // TODO Dispatch a change event
          break;

        case PokeyActions.EstimateSubmitted:
          console.log('estimate_submitted', action.roomId, action.estimate);
          PokeyApi.submitEstimate(action.roomId, action.estimate);
          break;

        case PokeyActions.NameSet:
          console.log('name_set', action.name);
          PokeyApi.setName(action.name);
          break;

        case PokeyActions.RoomCleared:
          PokeyApi.clearRoom(action.roomId);
          break;

        case PokeyActions.RoomCreated:
          PokeyApi.createRoom();
          break;

        case PokeyActions.RoomJoined:
          PokeyApi.joinRoom(action.roomId);
          break;

        case PokeyActions.RoomRevealed:
          PokeyApi.revealRoom(action.roomId);
          break;

        default:
          // do nothing
      }
    });

    PokeyApi.on(PokeyApiEvents.ConnectionInfo, () => {});
    PokeyApi.on(PokeyApiEvents.UserUpdated, () => {});
    PokeyApi.on(PokeyApiEvents.RoomCreated, () => {});
    PokeyApi.on(PokeyApiEvents.RoomUpdated, () => {});
    PokeyApi.on(PokeyApiEvents.UserJoined, () => {});
    PokeyApi.on(PokeyApiEvents.UserLeft, () => {});
    PokeyApi.on(PokeyApiEvents.EstimateUpdated, () => {});
    PokeyApi.on(PokeyApiEvents.RoomRevealed, () => {});
    PokeyApi.on(PokeyApiEvents.RoomCleared, () => {});
    PokeyApi.on(PokeyApiEvents.RoomClosed, () => {});
    PokeyApi.on(PokeyApiEvents.Error, () => {});

    PokeyApi.openConnection();

  }

  addChangeListener(callback) {
    this.addListener(EVENTS.CHANGE, callback);
  }

  removeChangeListener(callback) {
    this.removeListener(EVENTS.CHANGE, callback);
  }

  getCurrentUser() {
    return _currentUser;
  }

  getCurrentRoom() {
    return _currentRoom;
  }
}

export default new PokeyStore();
