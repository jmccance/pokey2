import React from 'react';

export default class extends React.Component {
  render() {
    return (
      <tr>
        <td>{this.props.name}</td>
        <td>
          <span>
            <span className="glyphicon glyphicon-ok text-success"></span>
          </span>
        </td>
        <td>
          &nbsp;
        </td>
      </tr>
    );
  }
}
