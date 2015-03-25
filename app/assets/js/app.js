import React from 'react';

import './router/router';
import PokeyApp from './components/PokeyApp';
import ClientAction from './connection/clientActions';
import Router from './router/router';

// Add React to the window for debugging porpoises.
window.React = React;

Router.init();
ClientAction.openConnection();

React.render(
  <PokeyApp />,
  document.getElementById('pokey')
);
