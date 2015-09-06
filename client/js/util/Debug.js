import Debug from 'debug';

const DEBUG_PREFIX = 'pokey';
Debug.enable(DEBUG_PREFIX + ':*');
window.debug = Debug;

function PokeyDebug(namespace) {
  return Debug(DEBUG_PREFIX + ':' + namespace);
}

export default PokeyDebug;
