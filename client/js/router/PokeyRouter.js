import director from 'director';

import PokeyActionCreator from '../actions/PokeyActionCreator';
import Views from '../models/Views';
import PokeyStore from '../stores/PokeyStore';

const PokeyRouter = new director.Router({
  '/': () => {
    console.log('router: /');
    PokeyActionCreator.viewChanged(Views.lobby);
  },

  '/room/:roomId': (roomId) => {
    console.log(`router: /room/${roomId}`);
    PokeyActionCreator.viewChanged(Views.room(roomId));
  }
});

export default PokeyRouter;
