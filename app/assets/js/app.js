import PokeyApp from './components/PokeyApp';
import React from 'react';

window.React = React;

React.render(
  <PokeyApp />,
  document.getElementById('pokey')
);
