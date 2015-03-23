import React from 'react';

export default class extends React.Component {
  render() {
    return (
      <form name="estimate"
            className='form-inline'
            data-role="form">

        <div className="form-group">
          <label htmlFor="estimate-hours">
              Estimate
          </label>

          <input id="estimate-hours"
                 className="form-control"
                 type="number"
                 min="1"
                 max="12"
                 step="1" />
        </div>

        <div className='form-group'>
          <label htmlFor="estimate-comment">
              Comment
          </label>

          <input id="estimate-comment"
                 className="form-control"
                 pattern=".*\w.*"
                 type="text"
                 maxLength="255"/>
        </div>

        <button className="btn btn-primary" type="submit">
            Submit
        </button>
      </form>
    );
  }
}
