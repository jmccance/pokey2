import React from 'react';
import Button from 'react-bootstrap/lib/Button';
import Modal from 'react-bootstrap/lib/Modal';

import PokeyActionCreator from '../actions/PokeyActionCreator';

export default class extends React.Component {
  onSubmit(event) {
    event.preventDefault();
    var name = React.findDOMNode(this.refs.name).value.trim();
    PokeyActionCreator.nameSet(name);
    this.props.close();
  }

  render() {
    return (
      <Modal {...this.props} title='Update Profile'>
        <form novalidate
              name='user'
              data-role='form'
              className='form-horizontal'
              onSubmit={e => this.onSubmit(e)}>
          <Modal.Header closeButton>
            <Modal.Title>Set Name</Modal.Title>
          </Modal.Header>
          <Modal.Body>
            <div className='form-group'>
              <label className='control-label col-sm-2' htmlFor='name'>
                Name
              </label>

              <div className='col-sm-10'>
                <input ref='name'
                       required
                       pattern='.*\w.*'
                       maxlength='32'
                       className='form-control'
                       type='text'/>
              </div>
            </div>
          </Modal.Body>

          <Modal.Footer>
            <Button bsStyle='primary' type='submit'>Register</Button>
          </Modal.Footer>
        </form>
      </Modal>
    );
  }
}
