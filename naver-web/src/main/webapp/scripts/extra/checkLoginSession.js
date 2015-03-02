'use strict';
pinpointApp.run([ '$rootScope', '$timeout', '$modal', '$location', '$cookies', '$interval',
    function ($rootScope, $timeout, $modal, $location, $cookies, $interval) {
        // initialize variables
        var bIsLoginModalOpened, oLoginModal;
        bIsLoginModalOpened = false;
        // initialize variables of methods
        var checkLoginSession;

        if ($location.host() === 'pinpoint.navercorp.com') {
            $timeout(function () {
                if (checkLoginSession() === false && bIsLoginModalOpened === false) {
                    oLoginModal.show();
                    bIsLoginModalOpened = true;
                }
            }, 700);
            $interval(function () {
                if (checkLoginSession() === false && bIsLoginModalOpened === false) {
                    oLoginModal.show();
                    bIsLoginModalOpened = true;
                } else if (checkLoginSession() === true && bIsLoginModalOpened === true) {
                    oLoginModal.hide();
                    bIsLoginModalOpened = false;
                }
            }, 3000);
        }

        checkLoginSession = function () {
            return angular.isDefined($cookies.SMSESSION);
        };

        oLoginModal = $modal({template: 'scripts/views/login.modal.html', backdrop: 'static', placement: 'center', show: false});

    }
]);
