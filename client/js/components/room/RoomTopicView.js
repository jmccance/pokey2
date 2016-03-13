import React from 'react';
import {Glyphicon, PageHeader} from 'react-bootstrap';

export default class extends React.Component {
  render() {
    const {topic} = this.props;

    let view;
    if (topic == null || topic.length > 0) {
      view = (<PageHeader>{topic}</PageHeader>);
    } else {
      view = (<span/>);
    }

    return view;
  }
}
