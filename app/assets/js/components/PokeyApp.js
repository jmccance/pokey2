import NavBar from './common/NavBar';
import React from 'react';
import WelcomeBox from './lobby/WelcomeBox';

export default class extends React.Component {
  render() {
    return (
      <div>
        <NavBar />
        <div className="container">
          <WelcomeBox />
        </div>
      </div>
    );
  }
}
