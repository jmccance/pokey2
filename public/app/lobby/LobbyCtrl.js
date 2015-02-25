define(function() {
  'use strict';

  return function(
      $location,
      $scope,
      pokeyService,
      registrationDialog
      ) {

    if (!pokeyService.isRegistered()) {
      if (pokeyService.isUsernameCached()) {
        pokeyService.register();
      } else {
        registrationDialog.show();
      }
    }

    // Handle room creation
    $scope.createRoom = function() {
      // Once the room is successfully created, redirect to it.
      pokeyService.one('roomCreated', function(room) {
        console.log('Room created. Navigating to it.');
        $scope.$apply(function() {
          $location.path('/room/' + room.id);
        });
      });

      pokeyService.createRoom();
    };
  };
});
