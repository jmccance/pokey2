define([
  'angular',
  'lobby/LobbyCtrl'
], function (
    angular,
    LobbyCtrl
    ) {
  'use strict';

  return angular.module('pokey.lobby.Lobby', ['ngRoute'])
      .config(['$routeProvider',
        function ($routeProvider) {
          $routeProvider.
              when('/', {
                templateUrl: 'app/lobby/lobby.html',
                controller: 'pokey.lobby.LobbyCtrl'
              });
        }])

      .controller('pokey.lobby.LobbyCtrl', [
        '$location',
        '$scope',
        'pokeyService',
        'registrationDialog',
        LobbyCtrl
      ]);
});
