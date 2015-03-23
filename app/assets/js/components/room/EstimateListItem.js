import React from 'react';

export default class extends React.Component {
  render() {
    const userName = 'Bob Name';
    const comment = 'Foo bar';
    return (
      <tr>
        <td>{userName}</td>
        <td>
          <span>
            <span className="glyphicon glyphicon-ok text-success"></span>
          </span>
        </td>
        <td>
          {comment}
        </td>
      </tr>
    );
  }
}
