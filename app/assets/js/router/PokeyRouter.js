import director from 'director';

import RouterAction from './routerActions'

const PokeyRouter = new director.Router({
  '/': () => {
    console.log('router: /');
    RouterAction.enteredLobby();
  },
  '/room/:roomId': (roomId) => {
    console.log(`router: /room/${roomId}`);
    RouterAction.enteredRoom(roomId);
  }
});

export default PokeyRouter;
