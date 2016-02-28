const PokeyApiEvents = {
  ConnectionInfo: 'CONNECTION_INFO',
  UserUpdated: 'USER_UPDATED',
  RoomCreated: 'ROOM_CREATED',
  RoomUpdated: 'ROOM_UPDATED',
  UserJoined: 'USER_JOINED',
  UserLeft: 'USER_LEFT',
  EstimateUpdated: 'ESTIMATE_UPDATED',
  RoomRevealed: 'ROOM_REVEALED',
  RoomCleared: 'ROOM_CLEARED',
  RoomClosed: 'ROOM_CLOSED',
  Error: 'ERROR',

  // Meta-API events.
  ConnectionOpened: 'CONNECTION_OPENED',
  ConnectionClosed: 'CONNECTION_CLOSED'
};

export default PokeyApiEvents;
