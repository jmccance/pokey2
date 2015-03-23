import React from 'react';

import EstimateListItem from './EstimateListItem';

export default class extends React.Component {
  render() {
    const estimates = [
      <EstimateListItem key={1} />,
      <EstimateListItem key={2} />,
      <EstimateListItem key={3} />
    ];

    return (
      <div>
        <table className="table table-striped">
          <tr>
            <th>User</th>
            <th>Estimate</th>
            <th>Comment</th>
          </tr>

          {estimates}
        </table>
      </div>
    );
  }
}
