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
  },

  viewChanged(view) {
    AppDispatcher.dispatch({
      type: PokeyActions.ViewChanged,
      view
    });
  }
};

export default PokeyActionCreator;
