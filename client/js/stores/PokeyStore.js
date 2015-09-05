import EventEmitter from 'events';
import _, { Map } from 'immutable';

import PokeyActions from '../actions/PokeyActions'
import PokeyApi from '../api/PokeyApi';
import PokeyApiEvents from '../api/PokeyApiEvents';
import AppDispatcher from '../dispatcher/appDispatcher';
import Room from '../models/Room';
import User from '../models/User';
import Views, { View } from '../models/Views';
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
          _currentRoom = new Room({ id: action.roomId });
          _view = Views.room(action.roomId);
          this.emitChange();
          break;

        case PokeyActions.RoomRevealed:
          debug('room_revealed %s', action.roomId);
          PokeyApi.revealRoom(action.roomId);
          break;

        case PokeyActions.ViewChanged:
          debug('view_changed %o', action.view);
          _view = action.view;

          if (_view instanceof View.Room) {
            _currentRoom = new Room({ id: _view.roomId });
            PokeyApi.joinRoom(_view.roomId);
          }

          this.emitChange();
          break;

        default:
          // do nothing
      }
    });

    PokeyApi
      .on(PokeyApiEvents.ConnectionInfo, (userId) => {
        debug('connection_info %s', userId);
        _currentUser = new User({ id: userId });
        this.emitChange();
      })
      .on(PokeyApiEvents.UserUpdated, (user) => {
        debug('user_updated %o', user);
        if (user.id == _currentUser.id) {
          _currentUser = new User(user);
        }
        this.emitChange();
      })
      .on(PokeyApiEvents.RoomCreated, (roomId) => {
        debug('room_created %s', roomId);
        _currentRoom = new Room({ id: roomId });
        _view = Views.room(roomId);
        this.emitChange();
      })
      .on(PokeyApiEvents.RoomUpdated, (room) => {
        if (room.id === _currentRoom.id) {
          debug('room_updated %o', room);
          _currentRoom = new Room(room);
          _view = Views.room(_currentRoom.id);
          this.emitChange();
        }
      })
      .on(PokeyApiEvents.UserJoined, (roomId, user) => {
        if (roomId === _currentRoom.id) {
          debug('user_joined %s, %o', roomId, user);
          _currentRoom = _currentRoom.update('users', (users) => {
            return users.set(user.id, new User(user));
          });
        }
      })
      .on(PokeyApiEvents.UserLeft, (roomId, user) => {
        if (roomId === _currentRoom.id) {
          debug('user_left %s, %o', roomId, user);
          _currentRoom = _currentRoom.update('users', (users) => {
            return users.remove(user.id);
          });
        }
      })
      .on(PokeyApiEvents.EstimateUpdated, () => {})
      .on(PokeyApiEvents.RoomRevealed, () => {})
      .on(PokeyApiEvents.RoomCleared, () => {})
      .on(PokeyApiEvents.RoomClosed, () => {})
      .on(PokeyApiEvents.Error, () => {});
  }

  init() {
    this.addChangeListener(() => {
      const currentRoute = PokeyRouter.getPath();
      const newView = this.getView();
      debug('update_route old=%s, new=%s', currentRoute, newView.route);

      if (newView !== null && newView.route !== currentRoute) {
        PokeyRouter.setRoute(newView.route);
      }
    });

    window.router = PokeyRouter;

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
