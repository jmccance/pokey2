import EventEmitter from 'events';
import * from './eventTypes';

export default class EventEmitter {
  constructor(url) {
    this.conn = new WebSocket(url);
    this.conn.onmessage = function (event) {
      switch (event.event) {
        case UserUpdated:
          break;

        case RoomCreated:
          break;
      }
    };
  }
}
