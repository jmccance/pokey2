import React from 'react';
import {Button, Jumbotron} from 'react-bootstrap';

import PokeyActionCreator from '../../actions/PokeyActionCreator';

export default class extends React.Component {
  render() {
    return (
      <Jumbotron>
        <h1>Welcome!</h1>

        <p>To get started, create a new room and send the URL out to your peers.</p>

        <p>
          <Button bsSize='large'
                  bsStyle='primary'
                  onClick={PokeyActionCreator.roomCreated}>
            Create Room
          </Button>
        </p>
      </Jumbotron>
    );
  }
}
