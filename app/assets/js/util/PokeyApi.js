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

        case RoomUpdatedEvent:
          break;

        case RoomUpdatedEvent:
          break;

        case UserJoinedEvent:
          break;

        case UserLeftEvent:
          break;

        case EstimateUpdatedEvent:
          break;

        case RoomRevealedEvent:
          break;

        case RoomClearedEvent:
          break;

        case RoomClosedEvent:
          break;

        case ErrorEvent:
          break;
      }
    };
  }

  setName(name) {
    const msg = {
      command: 'setName',
      name: name
    };

    this.conn.send(JSON.stringify(msg));
  }

  createRoom() {
    const msg = {
      command: 'createRoom'
    };

    this.conn.send(JSON.stringify(msg));
  }

  joinRoom(roomId) {
    const msg = {
      command: 'joinRoom',
      roomId: roomId
    };

    this.conn.send(JSON.stringify(msg));
  }

  submitEstimate(roomId, estimate) {
    const msg = {
      command: 'submitEstimate',
      roomId: roomId,
      estimate: estimate
    };

    this.conn.send(JSON.stringify(msg));
  }

  revealRoom(roomId) {
    const msg = {
      command: 'revealRoom',
      roomId: roomId
    };

    this.conn.send(JSON.stringify(msg));
  }

  clearRoom(roomId) {
    const msg = {
      command: 'clearRoom',
      roomId: roomId
    };

    this.conn.send(JSON.stringify(msg));
  }
}
