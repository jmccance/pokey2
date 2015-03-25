import AppDispatcher from '../dispatcher/appDispatcher';

import Connection from './connection';
import ConnectionEvent from './connectionEvent';

export default new class {
  openConnection() {
    let conn = new Connection();
    AppDispatcher.dispatch({
      type: ConnectionEvent.NewConnection,
      connection: conn
    });
  }
};
