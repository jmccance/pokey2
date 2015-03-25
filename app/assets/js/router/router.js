import director from 'director';

import RouterAction from './routerActions'

const routes = {
  '/': () => {
    console.log('router: /');
    RouterAction.enteredLobby();
  },
  '/room/:roomId': (roomId) => {
    console.log(`router: /room/${roomId}`);
    RouterAction.enteredRoom(roomId);
  }
};

export default new director.Router(routes);
