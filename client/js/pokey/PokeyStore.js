import EventEmitter from 'events';
import { Map } from 'immutable';

import AlertActionCreator from '../alerts/AlertActionCreator';
import Alerts from '../alerts/model/Alerts';
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

const AlertIdentifiers = {
  ConnectionClosed: 'CONNECTION_CLOSED'
};

const InternalEvents = {
  Change: 'CHANGE',
  Error: 'ERROR'
};

var _currentUser = null;
var _currentRoom = null;
var _isReconnecting = false;
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

        case PokeyActions.RoomRevealed:
          debug('room_revealed %s', action.roomId);
          PokeyApi.revealRoom(action.roomId);
          break;

        case PokeyActions.TopicSet:
          debug('topic_set %s, %s', action.roomId, action.topic);
          PokeyApi.setTopic(action.roomId, action.topic);
          break;

        case PokeyActions.ViewChanged:
          debug('view_changed %o', action.view);
          _view = action.view;

          if (_view instanceof View.Room) {
            _currentRoom = new Room({ id: _view.roomId });
            PokeyApi.joinRoom(_view.roomId);
          } else {
            _currentRoom = false;
          }

          this.emitChange();
          break;

        default:
          debug('unhandled_action %o', action);
      }
    });

    PokeyApi
      .on(PokeyApiEvents.ConnectionClosed, () => {
        debug('connection_closed');
        AlertActionCreator.alertCreated(
          Alerts.danger(
            'Connection lost. Attempting to reconnect...',
            AlertIdentifiers.ConnectionClosed
          )
        );
        _isReconnecting = true;
        PokeyApi.openConnection();
      })
      .on(PokeyApiEvents.ConnectionInfo, (userId) => {
        debug('connection_info %s', userId);
        _currentUser = new User({ id: userId });
        this.emitChange();
      })
      .on(PokeyApiEvents.ConnectionOpened, () => {
        debug('connection_opened');
        if (_isReconnecting) {
          _isReconnecting = false;

          AlertActionCreator.alertCreated(
            Alerts.success('Connection re-established.')
          );

          AlertActionCreator.allDismissed(AlertIdentifiers.ConnectionClosed);

          AppRouter.setRoute('/reconnected');
          AppRouter.setRoute(_view.route);
        }
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
      .on(PokeyApiEvents.RoomUpdated, (roomInfo) => {
        if (roomInfo.id === _currentRoom.id) {
          debug('room_updated %o', roomInfo);
          _currentRoom =
            _currentRoom
              .set('ownerId', roomInfo.ownerId)
              .set('topic', roomInfo.topic)
              .set('isRevealed', roomInfo.isRevealed);

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
      .on(PokeyApiEvents.Error, msg => {
        debug('error_received %s', msg);
        AlertActionCreator.alertCreated(Alerts.danger(msg));
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
