import React from 'react';

export default class extends React.Component {
  render() {
    return (
      <form name='estimate'
            data-role='form'>
        <div className='container-fluid'>
          <div className='row'>
            <div className='col-xs-1'>
              <label htmlFor='estimate-hours'>
                Estimate
              </label>
            </div>

            <div className='col-xs-2'>
              <input id='estimate-hours'
                     className='form-control'
                     type='number'
                     min='1'
                     max='12'
                     step='1' />
            </div>

            <div className='col-xs-1'>
              <label htmlFor='estimate-comment'>
                  Comment
              </label>
            </div>

            <div className='col-xs-7'>
              <input id='estimate-comment'
                     className='form-control'
                     pattern='.*\w.*'
                     type='text'
                     maxLength='255'/>
            </div>

            <div className='col-xs-1'>
              <button className='btn btn-primary' type='submit'>
                  Submit
              </button>
            </div>
          </div>
        </div>
      </form>
    );
  }
}
