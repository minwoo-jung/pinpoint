(function( $ ) {
	'use strict';
	/**
	 * (en)yobiDirective
	 * @ko yobiDirective
	 * @group Directive
	 * @name yobiDirective
	 * @class
	 */
	pinpointApp.directive( "yobiDirective", [ "$rootScope", "$http", "$window", "webStorage", "AnalyticsService",
		function ( $rootScope, $http, $window, webStorage, analyticsService ) {
			return {
				template: [
					'<div style="display:inline-block">',
					'<a href="https://yobi.navercorp.com/Labs-public_pinpoint-issues/posts" target="_blank" style="color:#FFF;" ng-click="openNotice()">',
					'<i class="xi-yobi" style="cursor:pointer;font-size:22px;"></i>',
					'<span class="glyphicon glyphicon-exclamation-sign" style="font-size:14px;top:-12px;left:-10px;color:#71FF1F" ng-show="hasNotice"></span>',
					'</a>',
					'</div>'
				].join(""),
				scope: {},
				restrict: 'EA',
				link: function ( scope ) {
					var aNoticeData = [];
					scope.hasNotice = true;
					$http.get( "https://yobi.navercorp.com/api/projects/pinpoint/posts?boardId=0a70564d-5c5f-16e7-815c-5f06e84c006f" ).success(function( aResult ) {
						var lastNoticeTime = webStorage.get("last-notice-time") || -1;

						aNoticeData = aResult.elements;
						if ( aNoticeData.length > 0 ) {
							if ( lastNoticeTime === -1 ) {
								scope.hasNotice = true;
							} else {
								var newNoticeTime = aNoticeData[0].lastUpdatedAt;
								if  ( newNoticeTime > lastNoticeTime ) {
									scope.hasNotice = true;
								} else {
									scope.hasNotice = false;
								}
							}
						}
					}).error( function() {

					});
					scope.openNotice = function() {
						if ( aNoticeData.length > 0 ) {
							webStorage.add("last-notice-time", aNoticeData[0].lastUpdatedAt);
						}
						analyticsService.send( analyticsService.CONST.MAIN, "ClickOpenYobi" );
						scope.hasNotice = false;
					};
				}
			};
		}
	]);
})( jQuery );
