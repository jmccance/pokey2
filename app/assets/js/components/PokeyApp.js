import React from 'react';
import Button from 'react-bootstrap/lib/Button';

import PokeyStore from '../stores/PokeyStore';
import PokeyRouter from '../router/PokeyRouter';
import Views, { Lobby, Room } from '../router/Views';
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
    const viewState = this.state.view;
    let view;

    if (viewState instanceof Lobby) {
      view = <LobbyView />
    } else if (viewState instanceof Room) {
      // TODO Validate whether or not we're the owner
      view = <RoomView isOwner={true} />;
    } else {
      // TODO Display a 404 page or redirect to Lobby.
      view = (<div className='container'><h1>404 :(</h1></div>);
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
      view: PokeyStore.getView()
    };
  }
}

export default PokeyApp;
