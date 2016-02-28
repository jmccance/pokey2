import EventEmitter from 'events';

import Debug from '../util/Debug';
import PokeyApiEvents from './PokeyApiEvents';

const debug = Debug('PokeyApi');

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
  constructor() {
    window.PokeyApi = this;
  }

  openConnection() {
    const url = _getUrl();
    debug('open_socket %s', url);
    this._messages = [];
    this.conn = new WebSocket(url);

    this.conn.onopen = () => {
      debug('connection_opened');
      const messages = this._messages;
      this._messages = [];
      messages.forEach((msg) => this._sendMessage(msg));
    };

    this.conn.onclose = () => {
      debug('connection_closed');
      this.emit(PokeyApiEvents.ConnectionClosed);
    };

    this.conn.onmessage = (message) => {
      const event = JSON.parse(message.data);
      debug('message_received %o', event);

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
    this._sendMessage({
      command: 'setName',
      name: name
    });
  }

  createRoom() {
    this._sendMessage({
      command: 'createRoom'
    });
  }

  joinRoom(roomId) {
    this._sendMessage({
      command: 'joinRoom',
      roomId: roomId
    });
  }

  submitEstimate(roomId, estimate) {
    this._sendMessage({
      command: 'submitEstimate',
      roomId: roomId,
      estimate: estimate
    });
  }

  clearRoom(roomId) {
    this._sendMessage({
      command: 'clearRoom',
      roomId: roomId
    });
  }

  revealRoom(roomId) {
    this._sendMessage({
      command: 'revealRoom',
      roomId: roomId
    });
  }

  killConnection() {
    this._sendMessage({
      command: 'killConnection'
    });
  }

  _sendMessage(msg) {
    if (this.conn.readyState === WebSocket.OPEN) {
      debug('send_message %o', msg);
      this.conn.send(JSON.stringify(msg));
    } else {
      debug('queue_message readyState=%d', this.conn.readyState);
      this._messages.push(msg);
    }
  }
}

export default new PokeyApi();
