import AppDispatcher from '../dispatcher/appDispatcher';
import Debug from '../util/Debug';
import PokeyActions from './PokeyActions';

const debug = Debug('pokey:PokeyActionCreator');

const PokeyActionCreator = {
  estimateSubmitted(roomId, estimate) {
    debug('estimateSubmitted %s, %o', roomId, estimate);
    AppDispatcher.dispatch({
      type: PokeyActions.EstimateSubmitted,
      roomId,
      estimate
    });
  },

  nameSet(name) {
    debug('nameSet %s', name);
    AppDispatcher.dispatch({
      type: PokeyActions.NameSet,
      name
    });
  },

  roomCleared(roomId) {
    debug('roomCleared %s', roomId);
    AppDispatcher.dispatch({
      type: PokeyActions.RoomCleared,
      roomId
    });
  },

  roomCreated() {
    debug('roomCreated');
    AppDispatcher.dispatch({
      type: PokeyActions.RoomCreated
    });
  },

  roomRevealed(roomId) {
    debug('roomRevealed %s', roomId);
    AppDispatcher.dispatch({
      type: PokeyActions.RoomRevealed,
      roomId
    });
  },

  topicSet(roomId, topic) {
    debug('topicSet %s, %s', roomId, topic);
    AppDispatcher.dispatch({
      type: PokeyActions.TopicSet,
      roomId,
      topic
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
