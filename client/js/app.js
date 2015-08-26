import React from 'react';

import PokeyApp from './components/PokeyApp';

// Add React to the window for debugging porpoises.
window.React = React;

React.render(
  <PokeyApp />,
  document.getElementById('pokey')
);
