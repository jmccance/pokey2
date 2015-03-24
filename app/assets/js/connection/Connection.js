import EventEmitter from 'events';
import * as EventType from './EventType';
import {ServerAction} from './ConnectionActions';

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

    this.conn.onmessage = function (event) {
      switch (event.event) {
        case EventType.UserUpdated:
          ServerAction.userUpdated(event.user);
          break;

        case EventType.RoomCreated:
          ServerAction.roomCreated(event.roomId);
          break;

        case EventType.RoomUpdatedEvent:
          ServerAction.roomUpdated(event.roomId, event.room);
          break;

        case EventType.UserJoinedEvent:
          ServerAction.userJoined(event.roomId, event.user);
          break;

        case EventType.UserLeftEvent:
          ServerAction.userLeft(event.roomId, event.user);
          break;

        case EventType.EstimateUpdatedEvent:
          ServerAction.estimateUpdated(event.roomId, event.userId, event.estimate);
          break;

        case EventType.RoomRevealedEvent:
          ServerAction.roomRevealed(event.roomId, event.estimates);
          break;

        case EventType.RoomClearedEvent:
          ServerAction.roomCleared(event.roomId);
          break;

        case EventType.RoomClosedEvent:
          ServerAction.roomClosed(event.roomId)
          break;

        case EventType.ErrorEvent:
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
