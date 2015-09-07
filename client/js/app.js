import React from 'react';

import PokeyApp from './components/PokeyApp';
import PokeyStore from './pokey/PokeyStore';

// Add React to the window for debugging porpoises.
window.React = React;

PokeyStore.init();

React.render(
  <PokeyApp />,
  document.getElementById('pokey')
);
