import React from 'react';
import {
  Button,
  ButtonInput,
  Col,
  FormGroup,
  Grid,
  Input,
  Row
} from 'react-bootstrap';

import PokeyActionCreator from '../../actions/PokeyActionCreator';

export default class extends React.Component {
  constructor(props) {
    super(props);

    this.onSubmit = (e) => {
      e.preventDefault();
      const estimate = {
        value: this.refs.estimate.getValue(),
        comment: this.refs.comment.getValue()
      };
      PokeyActionCreator.estimateSubmitted(props.roomId, estimate);
    }
  }

  render() {
    return (
      <form name='estimate'
            className='form-inline'
            id='estimate-form'
            data-role='form'
            onSubmit={this.onSubmit}>
          <Input ref='estimate'
                 type='number'
                 label='Estimate'
                 groupClassName='estimate' />
          <Input ref='comment'
                 type='text'
                 label='Comment'
                 groupClassName='comment' />
          <ButtonInput type='submit'
                       bsStyle='primary'
                       value='Submit'
                       groupClassName='submit-button'/>
      </form>
    );
  }
}
