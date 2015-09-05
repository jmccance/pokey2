import React from 'react';

import EstimateForm from './EstimateForm';
import EstimateHistogram from './EstimateHistogram';
import EstimateList from './EstimateList';
import RoomOwnerControls from './RoomOwnerControls';

export default class extends React.Component {
  render() {
    const props = this.props;

    return (
      <div className='container'>
        <div className='row'>
          <div className='col-md-10'>
            <EstimateForm />
          </div>

          <div className='col-md-2'>
            {this.props.isOwner ? <RoomOwnerControls /> : '&nbsp;'}
          </div>
        </div>

        <div className='row'>&nbsp;</div>

        <div className='row'>
          <div className='col-md-5'>
            <EstimateList users={props.room.users}
                          estimates={props.room.estimates}
                          isRevealed={props.room.isRevealed} />
          </div>

          <div className='col-md-7'>
             <EstimateHistogram />
          </div>
        </div>
      </div>
    );
  }
}
