require.config({
  baseUrl: 'app',
  paths: {
    angular: '../lib/angular/angular',
    angularBootstrap: '../lib/angular-bootstrap/ui-bootstrap-tpls',
    angularRoute: '../lib/angular-route/angular-route',
    angularCookies: '../lib/angular-cookies/angular-cookies',
    bean: '../lib/bean/bean',
    bootstrap: '../lib/bootstrap/dist/js/bootstrap',
    highcharts: 'http://code.highcharts.com/highcharts',
    jquery: '../lib/jquery/jquery',
    socketio: '../lib/socket.io-client/dist/socket.io',
    underscore: '../lib/underscore/underscore'
  },
  shim: {
    angular: {
      exports: 'angular'
    },
    angularBootstrap: ['angular'],
    angularCookies: ['angular'],
    angularRoute: ['angular'],
    bean: {
      exports: 'bean'
    },
    bootstrap: {
      deps: ['jquery']
    },
    highcharts: {
      deps: ['jquery'],
      exports: 'Highcharts'
    },
    socketio: {
      exports: 'io'
    },
    underscore: {
      exports: '_'
    }
  }
});

//http://code.angularjs.org/1.2.1/docs/guide/bootstrap#overview_deferred-bootstrap
window.name = 'NG_DEFER_BOOTSTRAP!';

require([
  'angular',
  'app',

  // Mixins
  'bootstrap'
], function(
    angular,
    app) {
  'use strict';
  angular.element(document.getElementsByTagName('html')[0]);

  angular.element().ready(function() {
    angular.resumeBootstrap([app.name]);
  });
});
