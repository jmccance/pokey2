import React from 'react';

import EstimateListItem from './EstimateListItem';

export default class extends React.Component {
  render() {
    const estimateListItems = this.props.users.map(user =>
      <EstimateListItem
        key={user.id}
        name={user.name}
        estimate={this.props.estimates.get(user.id)} />
    );

    return (
      <div>
        <table className='table table-striped'>
          <tr>
            <th>User</th>
            <th>Estimate</th>
            <th>Comment</th>
          </tr>

          {estimateListItems}
        </table>
      </div>
    );
  }
}
