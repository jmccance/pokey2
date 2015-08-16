import EventEmitter from 'events';

import PokeyApiEvents from './PokeyApiEvents';

function _getUrl() {
  const loc = window.location;
  let protocol;
  if (loc.protocol === 'https:') {
    protocol = 'wss:';
  } else {
    protocol = 'ws:';
  }

  return `${protocol}//${loc.host}/connect`;
}

class PokeyApi extends EventEmitter {
  openConnection() {
    this.conn = new WebSocket(_getUrl());

    this.conn.onmessage = (message) => {
      const event = JSON.parse(message.data);

      switch (event.event) {
        case 'connectionInfo':
          this.emit(PokeyApiEvents.ConnectionInfo, event.userId);
          break;

        case 'userUpdated':
          this.emit(PokeyApiEvents.UserUpdated, event.user);
          break;

        case 'roomCreated':
          this.emit(PokeyApiEvents.RoomCreated, event.roomId);
          break;

        case 'roomUpdated':
          this.emit(PokeyApiEvents.RoomUpdated, event.room);
          break;

        case 'userJoined':
          this.emit(PokeyApiEvents.UserJoined, event.roomId, event.user);
          break;

        case 'userLeft':
          this.emit(PokeyApiEvents.UserLeft, event.roomId, event.user);
          break;

        case 'estimateUpdated':
          this.emit(PokeyApiEvents.EstimateUpdated, event.roomId, event.userId, event.estimate);
          break;

        case 'roomRevealed':
          this.emit(PokeyApiEvents.RoomRevealed, event.roomId, event.estimates);
          break;

        case 'roomCleared':
          this.emit(PokeyApiEvents.RoomCleared, event.roomId);
          break;

        case 'roomClosed':
          this.emit(PokeyApiEvents.RoomClosed, event.roomId);
          break;

        case 'error':
          this.emit(PokeyApiEvents.Error, event.message);
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

  clearRoom(roomId) {
    const msg = {
      command: 'clearRoom',
      roomId: roomId
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
}

export default new PokeyApi();
