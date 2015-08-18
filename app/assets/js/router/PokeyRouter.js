import director from 'director';

import PokeyActionCreator from '../actions/PokeyActionCreator';
import Views from './Views';

const PokeyRouter = new director.Router({
  '/': () => {
    console.log('router: /');
    PokeyActionCreator.viewChanged({
      view: Views.Lobby
    });
  },
  '/room/:roomId': (roomId) => {
    console.log(`router: /room/${roomId}`);
    PokeyActionCreator.viewChanged({
      view: Views.Room,
      roomId: roomId
    });
  }
});

export default PokeyRouter;
