import EventEmitter from 'events';

import PokeyActions from '../actions/PokeyActions'
import PokeyApi from '../api/PokeyApi';
import PokeyApiEvents from '../api/PokeyApiEvents';
import AppDispatcher from '../dispatcher/appDispatcher';
import Views from '../models/Views';
import PokeyRouter from '../router/PokeyRouter';
import Debug from '../util/Debug';

const debug = Debug('PokeyStore');

const InternalEvents = {
  Change: 'CHANGE',
  Error: 'ERROR'
};

var _currentUser = null;
var _currentRoom = null;
var _view = null;

class PokeyStore extends EventEmitter {
  constructor() {
    super();

    // Constructor should perform wiring *only*. Active initialization (that makes stuff happen)
    // should be in init() below.

    AppDispatcher.register((action) => {
      switch (action.type) {
        case PokeyActions.EstimateSubmitted:
          debug('estimate_submitted %s, %o', action.roomId, action.estimate);
          PokeyApi.submitEstimate(action.roomId, action.estimate);
          break;

        case PokeyActions.NameSet:
          debug('name_set %s', action.name);
          PokeyApi.setName(action.name);
          break;

        case PokeyActions.RoomCleared:
          debug('room_cleared %s', action.roomId);
          PokeyApi.clearRoom(action.roomId);
          break;

        case PokeyActions.RoomCreated:
          debug('room_created');
          PokeyApi.createRoom();
          break;

        case PokeyActions.RoomJoined:
          debug('room_joined %s', action.roomId);
          PokeyApi.joinRoom(action.roomId);
          break;

        case PokeyActions.RoomRevealed:
          debug('room_revealed %s', action.roomId);
          PokeyApi.revealRoom(action.roomId);
          break;

        case PokeyActions.ViewChanged:
          debug('view_changed %o', action.view);
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
        debug('user_updated %o', user);
        _currentUser = user;
        this.emitChange();
      })
      .on(PokeyApiEvents.RoomCreated, (roomId) => {
        debug('room_created %s', roomId);
        PokeyApi.joinRoom(roomId);
      })
      .on(PokeyApiEvents.RoomUpdated, () => {})
      .on(PokeyApiEvents.UserJoined, (roomId, user) => {
        debug('user_joined %s, %o', roomId, user);
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
  }

  init() {
    this.addChangeListener(() => {
      const currentRoute = PokeyRouter.getRoute();
      const newView = this.getView();

      if (newView !== null && newView.route !== currentRoute) {
        PokeyRouter.setRoute(newView.route);
      }
    });

    PokeyApi.openConnection();
    PokeyRouter.init(Views.lobby.route);
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
