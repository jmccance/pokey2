import React from 'react';
import Highcharts from 'highcharts-browserify';

import Debug from '../../util/Debug';

const debug = Debug('components:room:EstimateHistogram');

export default class extends React.Component {
  componentDidMount() {
    debug('histogram_mounted');
    this.chart = new Highcharts.Chart({
      chart: {
        type: 'column',
        renderTo: React.findDOMNode(this)
      },
      title: { text: null },
      legend: { enabled: false },
      tooltip: { enabled: false },
      xAxis: {
        categories: this._categories(),
        title: { text: 'Estimate' }
      },
      yAxis: {
        title: { text: '# of votes' },
        tickInterval: 1
      },
      series: [
        {
          data: this._data()
        }
      ]
    });
  }

  componentDidUpdate() {
    debug('histogram_updated');
    this.chart.series[0].setData(this._data());
  }

  render() {
    return (
      <div></div>
    );
  }

  _categories() {
    return [0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, '> 13'];
  }

  _data() {
    const tally = this.props.estimates
      .filter(e => e)
      .countBy(e => {
        if (e.value <= 13) {
          return e.value;
        } else {
          return '> 13';
        }
      });

    const data = this._categories().map(c => tally.get(c.toString()) || 0);
    debug(data);
    return data;
  }
}
