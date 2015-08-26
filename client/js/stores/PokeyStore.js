import EventEmitter from 'events';

import PokeyActions from '../actions/PokeyActions'
import PokeyApi from '../api/PokeyApi';
import PokeyApiEvents from '../api/PokeyApiEvents';
import AppDispatcher from '../dispatcher/appDispatcher';
import Views from '../models/Views';

const InternalEvents = {
  Change: 'CHANGE',
  Error: 'ERROR'
};

var _currentUser = null;
var _currentRoom = null;
var _view = Views.lobby;

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
          console.log('room_cleared', action.roomId);
          PokeyApi.clearRoom(action.roomId);
          break;

        case PokeyActions.RoomCreated:
          console.log('room_created');
          PokeyApi.createRoom();
          break;

        case PokeyActions.RoomJoined:
          console.log('room_joined', action.roomId);
          PokeyApi.joinRoom(action.roomId);
          break;

        case PokeyActions.RoomRevealed:
          console.log('room_revealed', action.roomId);
          PokeyApi.revealRoom(action.roomId);
          break;

        case PokeyActions.ViewChanged:
          console.log('view_changed', action.view);
          _view = action.view;
          this.emitChange();
          break;

        default:
          // do nothing
      }
    });

    PokeyApi
      .on(PokeyApiEvents.ConnectionInfo, () => {})
      .on(PokeyApiEvents.UserUpdated, (user) => {
        console.log("user_updated", user);
        _currentUser = user;
        this.emitChange();
      })
      .on(PokeyApiEvents.RoomCreated, (roomId) => {
        console.log('room_created', roomId);
        PokeyApi.joinRoom(roomId);
      })
      .on(PokeyApiEvents.RoomUpdated, () => {})
      .on(PokeyApiEvents.UserJoined, (roomId, user) => {
        console.log('user_joined', roomId, user);
        // FIXME Don't change view unless _currentRoom.id != roomId
        if (user.id === _currentUser.id) {
          _view = Views.room(roomId);
          this.emitChange();
        }
      })
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
    return _currentUser;
  }

  getCurrentRoom() {
    return _currentRoom;
  }

  getView() {
    return _view;
  }
}

export default new PokeyStore();
