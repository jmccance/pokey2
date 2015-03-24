import EventEmitter from 'events';

import ContextAction from '../context/contextActions';
import User from '../users/user';
import {ServerAction} from './connectionActions';

function getUrl() {
  const loc = window.location;
  let protocol;
  if (loc.protocol === 'https:')  {
    protocol = 'wss:';
  } else {
    protocol = 'ws:';
  }

  return `${protocol}//${loc.host}/connect`;
}

export default class extends EventEmitter {
  constructor() {
    this.conn = new WebSocket(getUrl());

    this.conn.onmessage = (message) => {
      const event = JSON.parse(message.data);

      switch (event.event) {
        case 'connectionInfo':
          ContextAction.setCurrentUser(new User(event.userId))
          break;

        case 'userUpdated':
          ServerAction.userUpdated(event.user);
          break;

        case 'roomCreated':
          ServerAction.roomCreated(event.roomId);
          break;

        case 'roomUpdated':
          ServerAction.roomUpdated(event.roomId, event.room);
          break;

        case 'userJoined':
          ServerAction.userJoined(event.roomId, event.user);
          break;

        case 'userLeft':
          ServerAction.userLeft(event.roomId, event.user);
          break;

        case 'estimateUpdated':
          ServerAction.estimateUpdated(event.roomId, event.userId, event.estimate);
          break;

        case 'roomRevealed':
          ServerAction.roomRevealed(event.roomId, event.estimates);
          break;

        case 'roomCleared':
          ServerAction.roomCleared(event.roomId);
          break;

        case 'roomClosed':
          ServerAction.roomClosed(event.roomId)
          break;

        case 'error':
          ServerAction.error(event.message);
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
