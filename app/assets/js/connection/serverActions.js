import AppDispatcher from '../dispatcher/appDispatcher';
import ConnectionEvent from './connectionEvent';

export default new class {
  userUpdated(user) {
    AppDispatcher.dispatch({
      type: ConnectionEvent.UserUpdated,
      user: user
    });
  }

  roomCreated(roomId) {
    AppDispatcher.dispatch({
      type: ConnectionEvent.RoomCreated,
      roomId: roomId
    });
  }

  roomUpdated(room) {
    AppDispatcher.dispatch({
      type: ConnectionEvent.RoomUpdated,
      room: room
    });
  }

  userJoined(roomId, user) {
    AppDispatcher.dispatch({
      type: ConnectionEvent.UserJoined,
      roomId: roomId,
      user: user
    });
  }

  estimateUpdated(roomId, userId, estimate) {
    AppDispatcher.dispatch({
      type: ConnectionEvent.EstimateUpdated,
      roomId: roomId,
      userId: userId,
      estimate: estimate
    });
  }

  roomRevealed(roomId, estimates) {
    AppDispatcher.dispatch({
      type: ConnectionEvent.RoomRevealed,
      roomId: roomId,
      estimates: estimates
    });
  }

  roomCleared(roomId) {
    AppDispatcher.dispatch({
      type: ConnectionEvent.RoomCleared,
      roomId: roomId
    });
  }

  roomClosed(roomId) {
    AppDispatcher.dispatch({
      type: ConnectionEvent.RoomClosed,
      roomId: roomId
    });
  }

  error(message) {
    AppDispatcher.dispatch({
      type: ConnectionEvent.Error,
      message: message
    });
  }
};
