import { Record, Map} from 'immutable';

const Room = Record({
  id: null,
  ownerId: null,
  isRevealed: false,
  topic: null,
  users: Map(),
  estimates: Map()
});

export default Room;
