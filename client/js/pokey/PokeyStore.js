import EventEmitter from 'events';
import { Map } from 'immutable';

import AlertActionCreator from '../alerts/AlertActionCreator';
import Alert from '../alerts/model/Alert';
import PokeyApi from '../api/PokeyApi';
import PokeyApiEvents from '../api/PokeyApiEvents';
import AppDispatcher from '../dispatcher/appDispatcher';
import AppRouter from '../router/AppRouter';
import Debug from '../util/Debug';
import PokeyActions from './PokeyActions'
import Room from './model/Room';
import User from './model/User';
import Views, { View } from './model/Views';

const debug = Debug('pokey:PokeyStore');

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
          debug('unhandled_action %o', action);
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

        if (user.id === _currentUser.id) {
          _currentUser = new User(user);
        }

        if (_currentRoom && _currentRoom.users.has(user.id)) {
          _currentRoom = _currentRoom.update('users', users => users.set(user.id, user));
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
          _currentRoom = _currentRoom.update('users', users => users.set(user.id, new User(user)));
          this.emitChange();
        }
      })
      .on(PokeyApiEvents.UserLeft, (roomId, user) => {
        if (roomId === _currentRoom.id) {
          debug('user_left %s, %o', roomId, user);
          _currentRoom =
            _currentRoom
              .update('users', users => users.remove(user.id))
              .update('estimates', estimates => estimates.remove(user.id));
          this.emitChange();
        }
      })
      .on(PokeyApiEvents.EstimateUpdated, (roomId, userId, estimate) => {
        if (_currentRoom.id === roomId) {
          debug('estimate_updated %s, %s, %o', roomId, userId, estimate);

          _currentRoom =
            _currentRoom.update('estimates', estimates => estimates.set(userId, estimate));

          this.emitChange();
        }
      })
      .on(PokeyApiEvents.RoomRevealed, (roomId, estimates) => {
        if (_currentRoom.id === roomId) {
          debug('room_revealed %s, %o', roomId, estimates);

          _currentRoom =
            _currentRoom
              .set('isRevealed', true)
              .set('estimates', Map(estimates));

          this.emitChange();
        }
      })
      .on(PokeyApiEvents.RoomCleared, roomId => {
        debug('room_cleared %s', roomId);

        _currentRoom =
          _currentRoom
            .set('isRevealed', false)
            .set('estimates', Map());

        this.emitChange();
      })
      .on(PokeyApiEvents.RoomClosed, roomId => {
        debug('room_closed %s', roomId);
        this.emitChange();
      })
      .on(PokeyApiEvents.ConnectionClosed, msg => {
        debug('connection_closed');
        AlertActionCreator.alertCreated(
          new Alert({message: 'Lost connection to server. Please refresh the page.'}));
      })
      .on(PokeyApiEvents.Error, msg => {
        debug('error_received %s', msg);
        AlertActionCreator.alertCreated(new Alert({message: msg}));
      });
  }

  init() {
    this.addChangeListener(() => {
      const currentRoute = '/' + (AppRouter.getRoute().join('/'));
      const newView = this.getView();

      if (newView !== null && newView.route !== currentRoute) {
        debug('update_route old=%s, new=%s', currentRoute, newView.route);
        AppRouter.setRoute(newView.route);
      }
    });

    window.router = AppRouter;

    PokeyApi.openConnection();
    AppRouter.init(Views.lobby.route);
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

  getRoom() {
    return _currentRoom;
  }

  getView() {
    return _view;
  }
}

export default new PokeyStore();
