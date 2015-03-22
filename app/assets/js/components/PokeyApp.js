import director from 'director';
import React from 'react';

import {ContextStore, ContextType} from '../stores/context';
import NavBar from './common/NavBar';
import WelcomeBox from './lobby/WelcomeBox';

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
      view = <WelcomeBox />
    } else if (this.state.context == ContextType.room ){
      view = <h1>This would be Room {this.state.roomId}.</h1>
    }

    return (
      <div>
        <NavBar />
        <div className="container">
          {view}
        </div>
      </div>
    );
  }
}
