import React from 'react';

import PokeyApp from './components/PokeyApp';
import PokeyRouter from './router/PokeyRouter';

// Add React to the window for debugging porpoises.
window.React = React;

PokeyRouter.init();

React.render(
  <PokeyApp />,
  document.getElementById('pokey')
);
