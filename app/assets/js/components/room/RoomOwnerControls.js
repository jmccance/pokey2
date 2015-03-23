import React from 'react';

export default class extends React.Component {
  render() {
    return (
      <div className="btn-toolbar" data-role="toolbar">
        <div className="btn-group">
          <button type="button"
                  className="btn btn-info">
                  Reveal
          </button>
          <button type="button"
                  className="btn btn-warning">
            Clear
          </button>
        </div>
      </div>
    );
  }
}
