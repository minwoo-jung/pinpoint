'use strict';

pinpointApp.controller('LoginFormCtrl', [ '$scope',
    function ($scope) {

        $scope.refresh = function () {
            document.location.reload();
        };

        $scope.$on('timer-stopped', function (event, data){
            $scope.refresh();
        });
    }
]);
