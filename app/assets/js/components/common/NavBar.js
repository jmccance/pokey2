import React from 'react';

export default class extends React.Component {
  render() {
    return (
      <div className='navbar navbar-default'
           data-role='navigation'>
        <div className='container-fluid'>
          <div className='navbar-header'>
            <a href='#'>
              <span className='navbar-brand'>Pokey</span>
            </a>
          </div>
          <div className='collapse navbar-collapse'>
            <ul className='nav navbar-nav navbar-right'>
              <li><a>Change Name</a></li>
            </ul>
          </div>
        </div>
      </div>
    );
  }
}
