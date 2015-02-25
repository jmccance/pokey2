define([
  'angular',
  'alerts/AlertCtrl',
  'alerts/AlertService'
], function (
    angular,
    AlertCtrl,
    AlertService
    ) {
  'use strict';

  return angular.module('pokey.alerts.Alerts', [])
      .service('pokey.alerts.AlertService', [
        '$rootScope',
        'pokeyService',
        AlertService
      ])

      .controller('pokey.alerts.AlertCtrl', [
        '$scope',
        'pokey.alerts.AlertService',
        'socket',
        AlertCtrl
      ]);
});
