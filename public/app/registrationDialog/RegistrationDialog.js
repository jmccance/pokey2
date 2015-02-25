define(function () {
  'use strict';

  var RegistrationCtrl = function($scope, $modalInstance, pokeyService) {
    // Create a "sub-scope" of sorts since this is a "subcontroller". I don't pretend to understand
    // quite what's going on here.
    // See: http://stackoverflow.com/questions/18716113/scope-issue-in-angularjs-using-angularui-bootstrap-modal
    $scope.user = {};
    var user = pokeyService.getUser();
    $scope.user.name = user ? user.name : '';

    $scope.register = function (user) {
      pokeyService.one('registered', function() {
        $modalInstance.close();
      });
      console.log(user);
      pokeyService.register(user.name);
    };
  };

  var RegistrationDialog = function($modal, pokeyService) {
    this.$modal = $modal;
    this.pokeyService = pokeyService;
  };

  RegistrationDialog.prototype.show = function() {
    var self = this;
    this.$modal
        .open({
          templateUrl: 'app/registrationDialog/registrationDialog.html',
          backdrop: 'static',
          controller: ['$scope', '$modalInstance', 'pokeyService', RegistrationCtrl],
          resolve: {
            pokeyService: function () { return self.pokeyService; }
          }
        });
  };

  return RegistrationDialog;
});
