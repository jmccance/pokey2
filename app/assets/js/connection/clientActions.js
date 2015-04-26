import AppDispatcher from '../dispatcher/appDispatcher';

import Connection from './connection';
import ConnectionEvent from './connectionEvent';

export default new class {
  constructor() {
    this._conn = null;
  }

  openConnection() {
    this._conn = new Connection();
    AppDispatcher.dispatch({
      type: ConnectionEvent.NewConnection,
      connection: this._conn
    });
  }

  updateProfile(profile) {
    console.log("update_profile:", profile);
    this._conn.setName(profile.name);
  }
};
