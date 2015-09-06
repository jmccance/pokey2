import React from 'react';
import {Col, Grid, Row} from 'react-bootstrap';

export default class extends React.Component {
  render() {
    return (
      <form name='estimate'
            data-role='form'>
        <Grid fluid>
          <Row>
            <Col xs={1}>
              <label htmlFor='estimate-hours'>
                Estimate
              </label>
            </Col>

            <Col xs={2}>
              <input id='estimate-hours'
                     className='form-control'
                     type='number'
                     min='1'
                     max='12'
                     step='1' />
            </Col>

            <Col xs={1}>
              <label htmlFor='estimate-comment'>
                  Comment
              </label>
            </Col>

            <Col xs={7}>
              <input id='estimate-comment'
                     className='form-control'
                     pattern='.*\w.*'
                     type='text'
                     maxLength='255'/>
            </Col>

            <Col xs={1}>
              <button className='btn btn-primary' type='submit'>
                  Submit
              </button>
            </Col>
          </Row>
        </Grid>
      </form>
    );
  }
}
