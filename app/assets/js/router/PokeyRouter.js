import director from 'director';

import PokeyActionCreator from '../actions/PokeyActionCreator';
import PokeyStore from '../stores/PokeyStore';
import Views from './Views';

const PokeyRouter = new director.Router({
  '/': () => {
    console.log('router: /');
    PokeyActionCreator.viewChanged(Views.Lobby);
  },

  '/room/:roomId': (roomId) => {
    console.log(`router: /room/${roomId}`);
    PokeyActionCreator.viewChanged(Views.Room(roomId));
  }
});

PokeyStore.addChangeListener(() => {
  const currentRoute = PokeyRouter.getRoute();
  const newRoute = PokeyStore.getView().route;

  if (newRoute !== currentRoute) {
    PokeyRouter.setRoute(newRoute);
  }
});

PokeyRouter.init('/');

export default PokeyRouter;
