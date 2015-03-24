import director from 'director';
import React from 'react';

import NavBar from './common/NavBar';
import ContextStore, {View} from '../context/contextStore';
import ContextEvent from '../context/contextEvents';
import LobbyView from './lobby/LobbyView';
import RoomView from './room/RoomView';

export default class extends React.Component {
  constructor(props) {
    super(props);
    this.state = ContextStore.get();

    this._onChange = () => {
      let ctx = ContextStore.get();
      console.log('context_changed', ctx);
      this.setState(ctx);
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
    if (this.state.view == View.Lobby) {
      view = <LobbyView />
    } else if (this.state.view == View.Room) {
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
