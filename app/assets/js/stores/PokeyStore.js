import EventEmitter from 'events';

import PokeyActions from '../actions/PokeyActions'
import PokeyApi from '../api/PokeyApi';
import PokeyApiEvents from '../api/PokeyApiEvents';
import AppDispatcher from '../dispatcher/appDispatcher';

const EVENTS = {
  CHANGE: 'CHANGE',
  ERROR: 'ERROR'
};

var _user = null;
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

    PokeyApi
      .on(PokeyApiEvents.ConnectionInfo, () => {})
      .on(PokeyApiEvents.UserUpdated, (user) => {
        console.log("user_updated", user);
        _user = user;
        this.emitChange();
      })
      .on(PokeyApiEvents.RoomCreated, () => {})
      .on(PokeyApiEvents.RoomUpdated, () => {})
      .on(PokeyApiEvents.UserJoined, () => {})
      .on(PokeyApiEvents.UserLeft, () => {})
      .on(PokeyApiEvents.EstimateUpdated, () => {})
      .on(PokeyApiEvents.RoomRevealed, () => {})
      .on(PokeyApiEvents.RoomCleared, () => {})
      .on(PokeyApiEvents.RoomClosed, () => {})
      .on(PokeyApiEvents.Error, () => {});

    PokeyApi.openConnection();

  }

  emitChange() {
    this.emit(EVENTS.CHANGE);
  }

  addChangeListener(callback) {
    this.addListener(EVENTS.CHANGE, callback);
  }

  removeChangeListener(callback) {
    this.removeListener(EVENTS.CHANGE, callback);
  }

  getUser() {
    return _user;
  }

  getCurrentRoom() {
    return _currentRoom;
  }
}

export default new PokeyStore();
