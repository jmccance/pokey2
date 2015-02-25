define([
  // External dependencies
  'angular',
  'socketio',

  // Internal dependencies
  'api/PokeyService',
  'registrationDialog/RegistrationDialog',

  // Mix-ins
  'angularBootstrap',
  'angularCookies',
  'angularRoute',
  'alerts/Alerts',
  'lobby/Lobby',
  'navbar/NavBar',
  'room/Room'
], function (
    angular,
    socketio,
    PokeyService,
    RegistrationDialog
    ) {
  'use strict';

  var pokeyApp = angular.module('pokeyApp', [
    'ngRoute',
    'ngCookies',
    'ui.bootstrap',

    'pokey.alerts.Alerts',
    'pokey.lobby.Lobby',
    'pokey.room.Room',
    'pokey.navbar.NavBar'
  ]);

  pokeyApp
      .factory('sessionId', [
        '$cookies',
        function($cookies) {
          return $cookies['pokey.session'];
        }
      ])

      .factory('socket', function() {
        return socketio.connect();
      })

      .service('pokeyService', [
        '$cookies',
        'sessionId',
        'socket',
        PokeyService
      ])

      .service('registrationDialog', [
        '$modal',
        'pokeyService',
        RegistrationDialog
      ]);

  return pokeyApp;
});
