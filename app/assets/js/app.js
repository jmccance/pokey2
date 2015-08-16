import React from 'react';

import PokeyApp from './components/PokeyApp';
import Router from './router/router';

// Add React to the window for debugging porpoises.
window.React = React;

Router.init();

React.render(
  <PokeyApp />,
  document.getElementById('pokey')
);
