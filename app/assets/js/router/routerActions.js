import AppDispatcher from '../dispatcher/appDispatcher';

import Router from './router';
import RouterEvent from './routerEvents';

export default new class {
  goToLobby() {
    Router.setRoute('/');
  }

  enteredLobby() {
    AppDispatcher.dispatch({
      type: RouterEvent.EnteredLobby
    });
  }

  enteredRoom(roomId) {
    AppDispatcher.dispatch({
      type: RouterEvent.EnteredRoom,
      roomId: roomId
    });
  }
}
