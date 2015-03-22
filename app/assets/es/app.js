var PokeyApp = require('./components/PokeyApp.react');
var React = require('react');
window.React = React;

React.render(
  <PokeyApp />,
  document.getElementById('pokey')
);
