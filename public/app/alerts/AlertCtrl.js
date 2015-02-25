define(function () {
  'use strict';

  var AlertCtrl = function ($scope, alertService, socket) {
    console.log('alertService', alertService);
    $scope.alerts = alertService.getAlerts();

    // Clear alerts on location change.
    $scope.$on('$locationChangeSuccess', function () {
      $scope.alerts = [];
    });

    // When the alerts list changes, update the list.
    alertService.on('update', function () {
      $scope.$apply(function () {
        $scope.alerts = alertService.getAlerts();
      });
    });

    $scope.closeAlert = function (index) {
      alertService.remove(index);
    };

    socket.on('disconnect', function () {
      alertService.error('Connection to server lost. Try refreshing the page.');
    });
  };

  return AlertCtrl;
});
