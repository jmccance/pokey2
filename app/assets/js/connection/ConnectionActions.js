import AppDispatcher from '../dispatcher/AppDispatcher';
import * as EventType from './EventType';

export const ServerAction = new class {
  userUpdated(user) {
    AppDispatcher.dispatch({
      type: EventType.UserUpdated,
      user: user
    });
  }

  roomCreated(roomId) {
    AppDispatcher.dispatch({
      type: EventType.RoomCreated,
      roomId: roomId
    });
  }

  roomUpdated(room) {
    AppDispatcher.dispatch({
      type: EventType.RoomUpdated,
      room: room
    });
  }

  userJoined(roomId, user) {
    AppDispatcher.dispatch({
      type: EventType.UserJoined,
      roomId: roomId,
      user: user
    });
  }

  estimateUpdated(roomId, userId, estimate) {
    AppDispatcher.dispatch({
      type: EventType.EstimateUpdated,
      roomId: roomId,
      userId: userId,
      estimate: estimate
    });
  }

  roomRevealed(roomId, estimates) {
    AppDispatcher.dispatch({
      type: EventType.RoomRevealed,
      roomId: roomId,
      estimates: estimates
    });
  }

  roomCleared(roomId) {
    AppDispatcher.dispatch({
      type: EventType.RoomCleared,
      roomId: roomId
    });
  }

  roomClosed(roomId) {
    AppDispatcher.dispatch({
      type: EventType.RoomClosed,
      roomId: roomId
    });
  }

  error(message) {
    AppDispatcher.dispatch({
      type: EventType.Error,
      message: message
    });
  }
}
