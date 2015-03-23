import React from 'react';

import EstimateListItem from './EstimateListItem';

export default class extends React.Component {
  render() {
    let estimateListItems = [];
    for (let estimate of this.props.estimates) {
      estimateListItems.push(
        <EstimateListItem
          key={estimate.userId}
          name={estimate.name} />
      );
    }

    return (
      <div>
        <table className="table table-striped">
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
