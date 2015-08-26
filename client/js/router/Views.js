export class Lobby {
  get route() {
    return '/';
  }
}

export class Room {
  constructor(roomId) {
    this._roomId = roomId;
  }

  get route() {
    return `/room/${this._roomId}`;
  }

  get roomId() {
    return this._roomId;
  }
}

const Views = {
  lobby: new Lobby(),

  room: (roomId) => new Room(roomId)
};

export default Views;
