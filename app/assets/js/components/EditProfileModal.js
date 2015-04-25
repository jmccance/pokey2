import React from 'react';
import Button from 'react-bootstrap/lib/Button';
import Modal from 'react-bootstrap/lib/Modal';

export default class extends React.Component {
  render() {
    return (
      <Modal {...this.props} title='Update Profile'>
        <form novalidate name='user' data-role='form' className='form-horizontal'>
          <div className='modal-body'>
            <div className='form-group'>
              <label className='control-label col-sm-2' htmlFor='username'>
                Name
              </label>

              <div className='col-sm-10'>
                <input id='username'
                       required
                       pattern='.*\w.*'
                       maxlength='32'
                       className='form-control'
                       type='text'/>
              </div>
            </div>
          </div>

          <div className='modal-footer'>
            <Button bsStyle='primary' type='submit'>Register</Button>
          </div>
        </form>
      </Modal>
    );
  }
}
