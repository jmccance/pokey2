import React from 'react';
import Button from 'react-bootstrap/lib/Button';

import PokeyStore from '../stores/PokeyStore';
import PokeyRouter from '../router/PokeyRouter';
import Views from '../router/Views';
import LobbyView from './lobby/LobbyView';
import MainNav from './MainNav'
import RoomView from './room/RoomView';

class PokeyApp extends React.Component {
  constructor(props) {
    super(props);
    PokeyRouter.init();
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
    switch (this.state.view.view) {
      case Views.Lobby:
        view = <LobbyView />;
        break;

      case Views.Room:
        // TODO Validate whether or not we're the owner
        view = <RoomView isOwner={true} />;
        break;

      default:
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
