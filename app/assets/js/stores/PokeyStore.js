import EventEmitter from 'events';

import PokeyActions from '../actions/PokeyActions'
import PokeyApi from '../api/PokeyApi';
import PokeyApiEvents from '../api/PokeyApiEvents';
import AppDispatcher from '../dispatcher/appDispatcher';
import PokeyRouter from '../router/PokeyRouter';
import RouterEvents from '../router/routerEvents';

const InternalEvents = {
  Change: 'CHANGE',
  Error: 'ERROR'
};

/**
 * Context type identifiers. Indicates which view of the application is currently active.
 */
// TODO These constants should live somewhere more sensible.
export const View = {
  Lobby: 'lobby',
  Room: 'room'
};

var _user = null;
var _currentRoom = null;
var _view = null;

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

        case RouterEvents.EnteredLobby:
          _view = View.Lobby;
          this._emitChange();
          break;

        case RouterEvents.EnteredRoom:
          _view = View.Room;
          PokeyApi.joinRoom(action.roomId);
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
      .on(PokeyApiEvents.RoomCreated, () => {
        // TODO Emit change to trigger navigation to the room.
        this.emitChange();
      })
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
    this.emit(InternalEvents.Change);
  }

  addChangeListener(callback) {
    this.addListener(InternalEvents.Change, callback);
  }

  removeChangeListener(callback) {
    this.removeListener(InternalEvents.Change, callback);
  }

  getUser() {
    return _user;
  }

  getCurrentRoom() {
    return _currentRoom;
  }

  getView() {
    return _view;
  }
}

export default new PokeyStore();
