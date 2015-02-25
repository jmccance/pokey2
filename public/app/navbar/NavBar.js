define([
  'angular'
], function(angular) {
  'use strict';

  return angular.module('pokey.navbar.NavBar', [])
      .controller('pokey.navbar.NavBarCtrl', [
        '$scope',
        'registrationDialog',
        function($scope, registrationDialog) {
          $scope.changeName = function() {
            registrationDialog.show();
          };
        }]);
});
