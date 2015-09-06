import AppDispatcher from '../dispatcher/appDispatcher';
import Debug from '../util/Debug';
import PokeyActions from './PokeyActions';

const debug = Debug('PokeyActionCreator');

const PokeyActionCreator = {
  nameSet(name) {
    debug('nameSet %s', name);
    AppDispatcher.dispatch({
      type: PokeyActions.NameSet,
      name
    });
  },

  roomCreated() {
    debug('roomCreated');
    AppDispatcher.dispatch({
      type: PokeyActions.RoomCreated
    });
  },

  estimateSubmitted(roomId, estimate) {
    debug('submitEstimate %s, %o', roomId, estimate);
    AppDispatcher.dispatch({
      type: PokeyActions.EstimateSubmitted,
      roomId,
      estimate
    });
  },

  viewChanged(view) {
    debug('viewChanged %o', view);
    AppDispatcher.dispatch({
      type: PokeyActions.ViewChanged,
      view
    });
  }
};

export default PokeyActionCreator;
