import React from 'react';

import PokeyApp from './components/PokeyApp';
import Connection from './connection/Connection';
import AppDispatcher from './dispatcher/appDispatcher';
import Router from './router/router.js';

// Add React to the window for debugging porpoises.
window.React = React;

const conn = new Connection(AppDispatcher);

React.render(
  <PokeyApp />,
  document.getElementById('pokey')
);
