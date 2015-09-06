import React from 'react';
import {Table} from 'react-bootstrap';

import EstimateListItem from './EstimateListItem';

export default class extends React.Component {
  render() {
    const {
      users: users,
      estimates: estimates,
      isRevealed: isRevealed
    } = this.props;

    const estimateListItems = this.props.users.map(user =>
      <EstimateListItem
        name={user.name}
        estimate={estimates.get(user.id)}
        isRevealed={isRevealed} />
    );

    return (
      <div>
        <Table striped>
          <tr>
            <th>User</th>
            <th>Estimate</th>
            <th>Comment</th>
          </tr>

          {estimateListItems}
        </Table>
      </div>
    );
  }
}
