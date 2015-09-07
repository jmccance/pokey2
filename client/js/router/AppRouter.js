import director from 'director';

import PokeyActionCreator from '../pokey/PokeyActionCreator';
import PokeyStore from '../pokey/PokeyStore';
import Debug from '../util/Debug';
import Views from '../pokey/model/Views';

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
