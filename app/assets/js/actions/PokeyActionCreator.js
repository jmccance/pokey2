import PokeyAppDispatcher from '../dispatcher/PokeyAppDispatcher';
import PokeyConstants from '../constants/PokeyConstants';

const PokeyActionCreator = {
  applicationStarted() {
    PokeyAppDispatcher.dispatch({
      type: PokeyConstants.ActionTypes.APP_STARTED
    });
  }
};

export default PokeyActionCreator;
