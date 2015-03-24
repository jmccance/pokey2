import director from 'director';
import EventEmitter from 'events';

/**
 * Store for the current application context, including the current view to display (lobby or room) and the current
 * user.
 *
 * @module context/ContextStore
 */

/**
 * Context type identifiers. Indicates which view of the application is currently active.
 */
export const ContextType = {
  lobby: 'lobby',
  room: 'room'
};

var _context;

export default new class extends EventEmitter {
  constructor() {
    const routes = {
      '/': () => {
        console.log('router: /');
        _context = this._getContextFromRoute();
        this.emitChange();
      },
      '/room/:roomId': (roomId) => {
        console.log(`router: /room/${roomId}`);
        _context = this._getContextFromRoute();
        this._emitChange();
      }
    };

    this._router = director.Router(routes);
    this._router.init('');

    _context = this._getContextFromRoute();
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
    this.emit('route_updated');
  }

  /**
   * @returns {String} the current context string as determined by the current route
   */
  _getContextFromRoute() {
    const route = this._router.getRoute(0);

    let context;
    switch (route) {
      case '':
        context = { context: 'lobby' };
        break;

      case 'room':
        context = { context: 'room', roomId: this._router.getRoute(1) };
        break;
    }

    return context;
  }
}
