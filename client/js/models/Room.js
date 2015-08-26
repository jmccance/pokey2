class Room {
  constructor(id, ownerId, isRevealed) {
    this._id = id;
    this._ownerId = ownerId;
    this._isRevealed = isRevealed;
    this._users = new Map();
  }

  get id() { return this._id; }
  get ownerId() { return this._ownerId; }
  get isRevealed() { return this._isRevealed; }
  get users() { return new Map([...this._users]); }

  addUser(user) {
    this._users.set(user.id, user);
  }

  removeUser(user) {
    this._users.delete(user.id);
  }
}

export default Room;
