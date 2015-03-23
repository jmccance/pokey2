import React from 'react';

import EstimateForm from './EstimateForm';
import EstimateHistogram from './EstimateHistogram';
import EstimateList from './EstimateList';
import RoomOwnerControls from './RoomOwnerControls';

export default class extends React.Component {
  render() {
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

        <div className="row">&nbsp;</div>

        <div className="row">
          <div className='col-md-5'>
            <EstimateList />
          </div>

          <div className="col-md-7">
             <EstimateHistogram />
          </div>
        </div>
      </div>
    )
  }
}
