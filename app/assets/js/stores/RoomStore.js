import EventEmitter from 'events';

var _users = {};

class RoomStore extends EventEmitter {
  emitChange() {
    this.emit(CHANGE_EVENT);
  }
}

export default new RoomStore();
