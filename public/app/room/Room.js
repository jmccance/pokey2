define([
    'angular',
    'room/RoomCtrl'
], function(
    angular,
    RoomCtrl) {
  'use strict';

  return angular.module('pokey.room.Room', ['ngRoute'])
      .config(['$routeProvider',
        function ($routeProvider) {
          $routeProvider.
              when('/room/:roomId', {
                templateUrl: 'app/room/room.html',
                controller: 'RoomCtrl'
              });
        }])

      .controller('RoomCtrl', [
        '$routeParams',
        '$scope',
        'pokeyService',
        'registrationDialog',
        RoomCtrl
      ]);
});
