define([
  'jquery',
  'highcharts'
], function (
    $,
    Highcharts
    ) {
  'use strict';

  var MIN_HOURS = 1,
      MAX_HOURS = 12;

  var EstimateHistogram = function ($el) {
    this.chart = new Highcharts.Chart({
      chart: {
        type: 'column',
        renderTo: $el[0]
      },
      title: {
        text: null
      },
      legend: {
        enabled: false
      },
      xAxis: {
        categories: this._getDefaultCategories()
      },
      yAxis: {
        title: {
          text: '# of votes'
        },
        tickInterval: 1
      },
      series: [
        {
          data: this._getDefaultData()
        }
      ]
    });
  };

  /**
   * @param {Room} room
   */
  EstimateHistogram.prototype.update = function (room) {
    var tally = _.chain(room.members)
        .countBy(function (member) {
          if (member.estimate) {
            return member.estimate.hours;
          }
        })
        .omit(undefined)
        .value();

    var categories = [];
    var data = [];

    for (var i = 1; i <= 12; ++i) {
      categories.push(i + 'h');
      data.push(tally[i] ? tally[i] : 0);
    }

    this.chart.series[0].setData(data);
  };

  EstimateHistogram.prototype.clear = function () {
    this.chart.series[0].setData(this._getDefaultData());
  };

  EstimateHistogram.prototype._getDefaultCategories = function () {
    var categories = [];
    for (var i = MIN_HOURS; i <= MAX_HOURS; ++i) {
      categories.push(i + 'h');
    }
    return categories;
  };

  EstimateHistogram.prototype._getDefaultData = function () {
    var data = [];
    for (var i = MIN_HOURS; i <= MAX_HOURS; ++i) {
      data.push(0);
    }
    return data;
  };

  return EstimateHistogram;
});
