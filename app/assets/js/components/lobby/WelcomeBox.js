import React from 'react';

export default class extends React.Component {
  render() {
    return (
      <div className="jumbotron">
        <h1>Welcome!</h1>

        <p>To get started, create a new room and send the URL out to your peers.</p>

        <p>
          <a className="btn btn-primary btn-lg"
             data-role="button">
             Create Room
          </a>
        </p>
      </div>
    );
  }
}
