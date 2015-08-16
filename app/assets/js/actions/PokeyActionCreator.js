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
  }
};

export default PokeyActionCreator;
