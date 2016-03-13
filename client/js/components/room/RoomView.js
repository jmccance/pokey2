import React from 'react';
import {Glyphicon, PageHeader} from 'react-bootstrap';

import EstimateForm from './EstimateForm';
import EstimateHistogram from './EstimateHistogram';
import EstimateList from './EstimateList';
import RoomOwnerControls from './RoomOwnerControls';
import RoomTopicView from './RoomTopicView';
import RoomTopicOwnerView from './RoomTopicOwnerView';

const OWNER_CONTROLS_WIDTH = 2;

function formWidth(isOwner) {
  if (isOwner) {
    return `col-md-${12 - OWNER_CONTROLS_WIDTH}`;
  } else {
    return `col-md-12`;
  }
}

function roomOwnerControls(roomId) {
  return (
    <div className='col-md-2'>
      <RoomOwnerControls roomId={roomId} />
    </div>
  );
}

export default class extends React.Component {
  render() {
    const {isOwner, room} = this.props;

    let topicView;
    if (isOwner) {
      topicView = <RoomTopicOwnerView topic={room.topic} roomId={room.id}/>;
    } else {
      topicView = <RoomTopicView topic={room.topic}/>;
    }

    return (
      <div className='container'>
        {topicView}
        <div className='row'>
          <div className={formWidth(isOwner)}>
            <EstimateForm roomId={room.id}/>
          </div>

          {isOwner ? roomOwnerControls(room.id) : ''}
        </div>

        <div className='row'>&nbsp;</div>

        <div className='row'>
          <div className='col-md-5'>
            <EstimateList users={room.users}
                          estimates={room.estimates}
                          isRevealed={room.isRevealed} />
          </div>

          <div className='col-md-7'>
             { room.isRevealed
               ? <EstimateHistogram estimates={room.estimates} />
               : '' }
          </div>
        </div>
      </div>
    );
  }
}
