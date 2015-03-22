var EventEmitter = require('events').EventEmitter;

var _users = {};

export class UserStore extends EventEmitter {
  emitChange() {
    this.emit(CHANGE_EVENT);
  }
}
