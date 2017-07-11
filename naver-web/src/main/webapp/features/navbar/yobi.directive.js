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
					$http.get( "http://yobiadmin.navercorp.com:8080/posts/pinpoint-issues?notice=true" ).success(function( aResult ) {
						var lastNoticeTime = webStorage.get("last-notice-time") || -1;

						aNoticeData = aResult;
						if ( aResult.length > 0 ) {
							if ( lastNoticeTime === -1 ) {
								scope.hasNotice = true;
							} else {
								var aOld = lastNoticeTime.split("-");
								var aNew = aNoticeData[ aNoticeData.length - 1].createdDate.split("-");

								for ( var i = 0 ; i < aOld.length ; i++ ) {
									if  ( parseInt( aNew[i] ) > parseInt( aOld[i] ) ) {
										scope.hasNotice = true;
										break;
									}
								}
							}
						}
					}).error( function() {

					});
					scope.openNotice = function() {
						console.log( "openNotice");
						if ( aNoticeData.length > 0 ) {
							webStorage.add("last-notice-time", aNoticeData[aNoticeData.length - 1].createdDate);
						}
						analyticsService.send( analyticsService.CONST.MAIN, "ClickOpenYobi" );
						scope.hasNotice = false;
					};
				}
			};
		}
	]);
})( jQuery );
