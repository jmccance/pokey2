import React from 'react';
import Button from 'react-bootstrap/lib/Button';

import PokeyStore from '../stores/PokeyStore';
import { View } from '../stores/PokeyStore';
import LobbyView from './lobby/LobbyView';
import MainNav from './MainNav'
import RoomView from './room/RoomView';

class PokeyApp extends React.Component {
  constructor(props) {
    super(props);
    this.state = this._getState();

    this._onChange = () => {
      this.setState(this._getState());
    };
  }

  componentDidMount() {
    PokeyStore.addChangeListener(this._onChange);
  }

  componentWillUnmount() {
    PokeyStore.removeChangeListener(this._onChange);
  }

  render() {
    let view;
    switch (this.state.view) {
      case View.Lobby:
        view = <LobbyView />;
        break;

      case View.Room:
        // TODO Validate whether or not we're the owner
        view = <RoomView isOwner={true} />;
        break;

      default:
       // TODO Display a 404 page or redirect to Lobby.
    }

    return (
      <div>
        <MainNav user={this.state.user} />
        {view}
      </div>
    );
  }

  _getState() {
    return {
      user: PokeyStore.getUser(),
      view: View.Lobby
    };
  }
}

export default PokeyApp;
