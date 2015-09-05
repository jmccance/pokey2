import director from 'director';

import PokeyActionCreator from '../actions/PokeyActionCreator';
import Views from '../models/Views';
import PokeyStore from '../stores/PokeyStore';
import Debug from '../util/Debug';

const debug = Debug('PokeyRouter');

const PokeyRouter = new director.Router({
  '/': () => {
    debug('route_changed /');
    PokeyActionCreator.viewChanged(Views.lobby);
  },

  '/room/:roomId': (roomId) => {
    debug('route_changed %s', `/room/${roomId}`);
    PokeyActionCreator.viewChanged(Views.room(roomId));
  }
});

export default PokeyRouter;
