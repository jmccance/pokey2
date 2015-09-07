import director from 'director';

import Views from '../models/Views';
import PokeyActionCreator from '../pokey/PokeyActionCreator';
import PokeyStore from '../pokey/PokeyStore';
import Debug from '../util/Debug';

const debug = Debug('router:AppRouter');

const AppRouter = new director.Router({
  '/': () => {
    debug('route_changed /');
    PokeyActionCreator.viewChanged(Views.lobby);
  },

  '/room/:roomId': (roomId) => {
    debug('route_changed %s', `/room/${roomId}`);
    PokeyActionCreator.viewChanged(Views.room(roomId));
  }
});

export default AppRouter;
