export const View = {
  Lobby: class Lobby {
    get route() {
      return '/';
    }
  },

  Room: class Room {
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
}

const Views = {
  lobby: new View.Lobby(),

  room: (roomId) => new View.Room(roomId)
};

export default Views;
