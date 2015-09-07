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

import PokeyActionCreator from '../../pokey/PokeyActionCreator';

export default class extends React.Component {
  constructor(props) {
    super(props);

    this.state = {
      value: '',
      comment: ''
    };
  }

  render() {
    const onChange = () => {
      this.setState({
        value: this.refs.value.getValue(),
        comment: this.refs.comment.getValue()
      });
    };

    const onSubmit = e => {
      e.preventDefault();
      const estimate = {
        value: this.refs.value.getValue(),
        comment: this.refs.comment.getValue()
      };
      PokeyActionCreator.estimateSubmitted(this.props.roomId, estimate);
      this.resetState();
    };

    return (
      <form name='estimate'
            className='form-inline'
            id='estimate-form'
            data-role='form'
            onSubmit={onSubmit}>
          <Input ref='value'
                 type='number'
                 label='Estimate'
                 groupClassName='value'
                 value={this.state.value}
                 onChange={onChange}/>
          <Input ref='comment'
                 type='text'
                 label='Comment'
                 groupClassName='comment'
                 value={this.state.comment}
                 onChange={onChange} />
          <ButtonInput type='submit'
                       bsStyle='primary'
                       value='Submit'
                       groupClassName='submit-button'/>
      </form>
    );
  }

  resetState() {
    this.setState({value: '', comment: ''});
  }
}
