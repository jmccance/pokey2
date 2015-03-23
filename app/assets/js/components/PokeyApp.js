import director from 'director';
import React from 'react';

import {ContextStore, ContextType} from '../stores/context';
import NavBar from './common/NavBar';
import LobbyView from './lobby/LobbyView';
import RoomView from './room/RoomView';

export default class extends React.Component {
  constructor(props) {
    super(props);
    this.state = ContextStore.get();
    ContextStore.addListener(
      'route_updated',
      () => this.setState(ContextStore.get())
    );
  }

  render() {
    let view;
    if (this.state.context == ContextType.lobby) {
      view = <LobbyView />
    } else if (this.state.context == ContextType.room ){
      view = <RoomView isOwner={true} />
    }

    return (
      <div>
        <NavBar />
        {view}
      </div>
    );
  }
}
