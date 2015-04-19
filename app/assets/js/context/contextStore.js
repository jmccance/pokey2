import director from 'director';
import EventEmitter from 'events';

import AppDispatcher from '../dispatcher/appDispatcher';
import RouterEvent from '../router/routerEvents';

import ContextEvent from './contextEvents';

/**
 * Store for the current application context, including the current view to display (lobby or room) and the current
 * user.
 *
 * @module context/ContextStore
 */

/**
 * Context type identifiers. Indicates which view of the application is currently active.
 */
export const View = {
  Lobby: 'lobby',
  Room: 'room'
};

var _context = { view: View.Lobby };

export default new class extends EventEmitter {
  constructor() {
    this.dispatchToken = AppDispatcher.register( (action) => {
      switch(action.type) {
        case ContextEvent.UserChanged:
          _context.user = action.user;
          this._emitChange();
          break;

        case RouterEvent.EnteredLobby:
          _context.view = View.Lobby;
          _context.roomId = null;
          this._emitChange();
          break;

        case RouterEvent.EnteredRoom:
          _context.view = View.Room;
          _context.roomId = action.roomId;
          this._emitChange();
          break;
      }
    });
  }

  /**
   * @returns {String} the current context identifier
   */
  get() {
    return _context;
  }

  /**
   * Notify listeners that the store has changed.
   */
  _emitChange() {
    console.log('ContextStore', ContextEvent.ContextChanged, _context);
    this.emit(ContextEvent.ContextChanged);
  }
}
