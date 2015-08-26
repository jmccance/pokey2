import React from 'react';
import Button from 'react-bootstrap/lib/Button';
import Jumbotron from 'react-bootstrap/lib/Jumbotron';

import PokeyActionCreator from '../../actions/PokeyActionCreator';

export default class extends React.Component {
  render() {
    return (
      <div className='container'>
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
      </div>
    );
  }
}
