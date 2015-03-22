import EventEmitter from 'events';

export default class UserStore extends EventEmitter {
  constructor() {
    this._users = new Map();
  }

  emitChange() {
    this.emit(CHANGE_EVENT);
  }
}
