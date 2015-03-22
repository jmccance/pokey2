import {EventEmitter} from 'events';

var _users = {};

class UserStore extends EventEmitter {
  emitChange() {
    this.emit(CHANGE_EVENT);
  }
}

export default new UserStore();
