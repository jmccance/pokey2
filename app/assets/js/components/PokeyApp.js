import director from 'director';
import React from 'react';
import Button from 'react-bootstrap/lib/Button';

import ContextStore, {View} from '../context/contextStore';
import ContextEvent from '../context/contextEvents';
import LobbyView from './lobby/LobbyView';
import MainNav from './MainNav'
import PokeyStore from '../stores/PokeyStore';
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
    if (this.state.view === View.Lobby) {
      view = <LobbyView />;
    } else if (this.state.view === View.Room) {
      view = <RoomView isOwner={true} />;
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
