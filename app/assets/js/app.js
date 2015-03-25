import React from 'react';

import PokeyApp from './components/PokeyApp';
import {ClientAction} from './connection/connectionActions';

// Add React to the window for debugging porpoises.
window.React = React;

ClientAction.openConnection();

React.render(
  <PokeyApp />,
  document.getElementById('pokey')
);
