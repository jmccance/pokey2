import director from 'director';
import React from 'react';
import Button from 'react-bootstrap/lib/Button';

import MainNav from './MainNav'
import ContextStore, {View} from '../context/contextStore';
import ContextEvent from '../context/contextEvents';
import LobbyView from './lobby/LobbyView';
import RoomView from './room/RoomView';

export default class extends React.Component {
  constructor(props) {
    super(props);
    this.state = this._getState();

    this._onChange = () => {
      this.setState(this._getState());
    };
  }

  componentDidMount() {
    ContextStore.on(
      ContextEvent.ContextChanged,
      this._onChange
    );
  }

  componentWillUnmount() {
    ContextStore.removeListener(this._onChange);
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
        <MainNav />
        {view}
      </div>
    );
  }

  _getState() {
    let ctx = ContextStore.get();
    console.log('context_changed', ctx);
    return ctx;
  }
}
