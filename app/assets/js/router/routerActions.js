import AppDispatcher from '../dispatcher/appDispatcher';

import PokeyRouter from './PokeyRouter';
import RouterEvent from './routerEvents';

export default new class {
  goToLobby() {
    PokeyRouter.setRoute('/');
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
