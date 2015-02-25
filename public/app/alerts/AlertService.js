define([
  'bean',
  'underscore'
], function (
    bean,
    _
    ) {
  'use strict';

  var AlertService = function ($rootScope, pokeyService) {
    var self = this;
    self.$rootScope = $rootScope;
    self.alerts = [];

    pokeyService.on('error', function (message) {
      self.error(message);
    });
  };

  var alert = function (type, message) {
    this.alerts.push({
      type: type,
      message: message
    });
    this.fire('update');
  };

  AlertService.prototype = {
    error: _.partial(alert, 'danger'),
    warn: _.partial(alert, 'warning'),
    info: _.partial(alert, 'info'),
    success: _.partial(alert, 'success'),

    getAlerts: function () {
      return this.alerts;
    },

    remove: function (index) {
      this.alerts.splice(index, 1);
    }
  };

  // Delegate the event handler methods to bean.
  _.each(['on', 'one', 'off', 'fire'], function (method) {
    AlertService.prototype[method] = function () {
      Array.prototype.unshift.call(arguments, this);
      bean[method].apply(undefined, arguments);
    };
  });

  return AlertService;
});
