import React from 'react';

import PokeyApp from './components/PokeyApp';

window.React = React;

React.render(
  <PokeyApp />,
  document.getElementById('pokey')
);
