import AppDispatcher from '../dispatcher/appDispatcher';
import PokeyActions from './PokeyActions';

const PokeyActionCreator = {
  appStarted() {
    AppDispatcher.dispatch({
      type: PokeyActions.AppStarted
    });
  },

  nameSet(name) {
    AppDispatcher.dispatch({
      type: PokeyActions.NameSet,
      name
    });
  },

  roomCreated() {
    AppDispatcher.dispatch({
      type: PokeyActions.RoomCreated
    });
  }
};

export default PokeyActionCreator;
