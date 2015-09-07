import React from 'react';

import PokeyActionCreator from '../../pokey/PokeyActionCreator';

export default class extends React.Component {
  render() {
    const roomId = this.props.roomId;

    const onClear = () => {
      PokeyActionCreator.roomCleared(roomId);
    };

    const onReveal = () => {
      PokeyActionCreator.roomRevealed(roomId);
    };

    return (
      <div className='btn-toolbar' data-role='toolbar'>
        <div className='btn-group'>
          <button type='button'
                  className='btn btn-info'
                  onClick={onReveal}>
                  Reveal
          </button>
          <button type='button'
                  className='btn btn-warning'
                  onClick={onClear}>
            Clear
          </button>
        </div>
      </div>
    );
  }
}
