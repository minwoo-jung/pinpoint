(function( $ ) {
	"use strict";

	pinpointApp.directive( "applicationConfigDirective", [ "globalConfig",
		function ( globalConfig ) {
			return {
				restrict: "EA",
				replace: true,
				templateUrl: "features/configuration/application/applicationConfig.html?v=" + G_BUILD_TIME,
				link: function( scope, element, attr ) {
					var $element = element;
					var myName = attr["name"];
					var invokeCountAfterOpen = 0;
					$element[ attr["initState"] ]();

					scope.$on( "configuration.selectMenu", function( event, selectedName ) {
						if ( myName === selectedName ) {
							$element.show();
							scope.$broadcast( "applicationGroup.load" );
						} else {
							$element.hide();
							invokeCountAfterOpen = 0;
						}
					});
					scope.$on( "applicationGroup.selectApp", function( event, appId ) {
						scope.$broadcast( "applicationGroup.sub.load", appId, invokeCountAfterOpen++ );
						event.stopPropagation();
					});
					scope.$on( "changed.role", function( event, bIsManager ) {
						scope.$broadcast( "change.your.role", scope.openSource ? true : bIsManager );
						event.stopPropagation();
					});
					scope.isInnerMode = function() {
						return globalConfig.openSource === false ? true : false;
					};
				}
			};
		}
	]);
})( jQuery );