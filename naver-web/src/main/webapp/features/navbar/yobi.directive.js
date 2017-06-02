(function( $ ) {
	'use strict';
	/**
	 * (en)yobiDirective
	 * @ko yobiDirective
	 * @group Directive
	 * @name yobiDirective
	 * @class
	 */
	pinpointApp.directive( "yobiDirective", [ "$rootScope", "$http", "webStorage", "AnalyticsService",
		function ( $rootScope, $http, webStorage, analyticsService ) {
			return {
				template: [
					'<div style="display:inline-block">',
						'<i class="xi-yobi" style="cursor:pointer;font-size:22px;font-weight:bold;" ng-click="openNotice()" ng-show="hasNotice"></i>',
						'<span class="glyphicon glyphicon-exclamation-sign" style="cursor:pointer;font-size:14px;top:-12px;left:-10px;color:#71FF1F" ng-show="hasNotice"></span>',
						'<i class="xi-yobi" style="cursor:pointer;font-size:22px;" ng-click="openNotice()" ng-hide="hasNotice"></i>',
					'</div>'
				].join(""),
				scope: {},
				restrict: 'EA',
				link: function ( scope ) {
					var aNoticeData = [];
					scope.hasNotice = false;
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
						if ( aNoticeData.length > 0 ) {
							webStorage.add("last-notice-time", aNoticeData[aNoticeData.length - 1].createdDate);
						}
						window.open( "https://yobi.navercorp.com/Labs-public_pinpoint-issues/posts" );
						analyticsService.send( analyticsService.CONST.MAIN, "ClickOpenYobi" );
						scope.hasNotice = false;
					};
				}
			};
		}
	]);
})( jQuery );
