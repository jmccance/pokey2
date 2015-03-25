import React from 'react';

export default class extends React.Component {

  render() {
    return (
      <div className='modal fade' id='update-user-modal'>
        <div className='modal-dialog'>
          <div className='modal-content'>
            <div className='modal-header'>
              <h4 className='modal-title'>Register</h4>
            </div>
            <form novalidate name='user' data-role='form'>
              <div className='modal-body'>
                <div className='form-group'>
                  <label className='control-label' htmlFor='username'>
                    What would you like to be called?
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
                <button className='btn btn-primary' type='submit'>
                  Register
                </button>
              </div>
            </form>
          </div>
        </div>
      </div>
    );
  }
}
