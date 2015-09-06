import React from 'react';
import {Glyphicon} from 'react-bootstrap';

export default class extends React.Component {
  render() {
    const {
      name: name,
      estimate: estimate,
      isRevealed: isRevealed
    } = this.props;

    let commentComp = '';
    let estimateComp = '';
    if (isRevealed) {
      estimateComp = estimate.value;
      commentComp = estimate.comment;
    } else if (estimate) {
      estimateComp = <Glyphicon glyph='ok' className='text-success' />;
    }

    return (
      <tr>
        <td>{name}</td>
        <td>{estimateComp}</td>
        <td>{commentComp}</td>
      </tr>
    );
  }
}
