import EventEmitter from 'events';

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

const PokeyApi = {
  openConnection() {
    this.conn = new WebSocket(_getUrl());

    // TODO Set up server-side event responses.
  },

  setName(name) {
    const msg = {
      command: 'setName',
      name: name
    };

    this.conn.send(JSON.stringify(msg));
  },

  createRoom() {
    const msg = {
      command: 'createRoom'
    };

    this.conn.send(JSON.stringify(msg));
  },

  joinRoom(roomId) {
    const msg = {
      command: 'joinRoom',
      roomId: roomId
    };

    this.conn.send(JSON.stringify(msg));
  },

  submitEstimate(roomId, estimate) {
    const msg = {
      command: 'submitEstimate',
      roomId: roomId,
      estimate: estimate
    };

    this.conn.send(JSON.stringify(msg));
  },

  revealRoom(roomId) {
    const msg = {
      command: 'revealRoom',
      roomId: roomId
    };

    this.conn.send(JSON.stringify(msg));
  },

  clearRoom(roomId) {
    const msg = {
      command: 'clearRoom',
      roomId: roomId
    };

    this.conn.send(JSON.stringify(msg));
  }
};

export default PokeyApi;
