import director from 'director';
import EventEmitter from 'events';

export const ContextType = {
  lobby: 'lobby',
  room: 'room'
};

export default new class extends EventEmitter {
  constructor() {
    const routes = {
      '/': () => {
        console.log('router: /');
        this._context = this._getContextFromRoute();
        this.emitChange();
      },
      '/room/:roomId': (roomId) => {
        console.log(`router: /room/${roomId}`);
        this._context = this._getContextFromRoute();
        this.emitChange();
      }
    };

    this._router = director.Router(routes);
    this._router.init('');

    this._context = this._getContextFromRoute();
  }

  emitChange() {
    this.emit('route_updated');
  }

  get() {
    return this._context;
  }

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
