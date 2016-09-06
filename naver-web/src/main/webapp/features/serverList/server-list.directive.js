(function( $ ) {
	'use strict';
	/**
	 * (en)serverListDirective
	 * @ko serverListDirective
	 * @group Directive
	 * @name serverListDirective
	 * @class
	 */
	pinpointApp.directive( "serverListDirective", [ "$http", "$timeout", "$window", "AnalyticsService", "TooltipService",
		function ( $http, $timeout, $window, analyticsService, tooltipService ) {
			return {
				restrict: "EA",
				replace: true,
				templateUrl: "features/serverList/serverList.html?v=" + G_BUILD_TIME,
				scope: {
					namespace: "@"
				},
				link: function(scope, element) {
					var bVisible = false;
					var bInitialized = false;
					var bAjaxLoading = false;
					var $element = $(element);
					scope.bIsNode = true;
					scope.hasScatter = false;
					scope.selectedAgent = "";
					/*

					 tooltipService.init( "serverList" );
					 */
					var showLayer = function() {
						$element.animate({
							"right": 421
						}, 500, function() {});
					};
					var showChart = function( instanceName, histogram, timeSeriesHistogram ) {
						scope.selectedAgent = instanceName;
						scope.$broadcast('changedCurrentAgent.forServerList', instanceName );
						if ( bInitialized ) {
							scope.$broadcast('responseTimeChartDirective.updateData.forServerList', histogram);
							scope.$broadcast('loadChartDirective.updateData.forServerList', timeSeriesHistogram);
						} else {
							scope.$broadcast('responseTimeChartDirective.initAndRenderWithData.forServerList', histogram, '360px', '180px', false, true);
							scope.$broadcast('loadChartDirective.initAndRenderWithData.forServerList', timeSeriesHistogram, '360px', '200px', false, true);
							bInitialized = true;
						}
					};
					scope.hideLayer = function( delay ) {
						delay = delay || 100;
						$element.animate({
							"right": -386
						}, delay, function() {
							bVisible = false;
							hideNmsLayer();
						});
					};
					scope.hasError = function( instance ) {
						return (instance && instance.Error && instance.Error > 0 ) ? "red": "";
					};
					scope.openInspector = function( $event, instanceName ) {
						$event.preventDefault();
						analyticsService.send( analyticsService.CONST.MAIN, analyticsService.CONST.CLK_OPEN_INSPECTOR );
						$window.open( "#/inspector/" + ( scope.node.applicationName || scope.node.filterApplicationName ) + "@" + ( scope.node.serviceType || "" ) + "/" + scope.oNavbarVoService.getReadablePeriod() + "/" + scope.oNavbarVoService.getQueryEndDateTime() + "/" + instanceName );
					};
					scope.selectServer = function( instanceName ) {
						if ( scope.bIsNode ) {
							showChart( instanceName, scope.node.agentHistogram[instanceName], scope.node.agentTimeSeriesHistogram[instanceName] );
						} else {
							showChart( instanceName, scope.node.sourceHistogram[instanceName], scope.node.sourceTimeSeriesHistogram[instanceName] );
						}
					};

					// <!--for NMS
					scope.showNMSList = false;
					var $nms = $element.find(".nms");
					var compiledTemplate = Handlebars.compile([
						'<div>',
						'{{#each datum}}',
							'<div>',
							'{{#each this}}',
								'<h4 style="color:#C36B05;padding-left:2px"> <span class="glyphicon glyphicon-globe" aria-hidden="true"></span> {{name}} - {{ip}} : {{port}}</h4>',
								'{{#each image}}',
									'<dl>',
										'<dt style="padding-left:2px"> <span class="glyphicon glyphicon-fullscreen" aria-hidden="true"></span> {{title}}</dt>',
										'<dd><a href="{{url}}" target="_blank"><img src="{{url}}" width="390" height="146"></a></dd>',
									'</dl>',
								'{{/each}}',
							'{{/each}}',
							'</div>',
						'{{/each}}',
						'</div>'
					].join(''));
					function hideNmsLayer() {
						uncheckAllNms();
						$nms.parent().scrollTop(0);
						scope.showNMSList = false;
						bAjaxLoading = false;
					}
					function uncheckAllNms( current ) {
						$element.find( "input[type=checkbox]" ).each(function(index, ele) {
							if ( angular.isDefined( current ) && current == $(ele).prop("name") ) {
							} else {
								$(ele).removeAttr("checked");
							}
						});
					}
					scope.openSite = function( $event, type, name, url ) {
						switch( type ) {
							case "aTag":
								$window.open( url );
								break;
							case "button":
								if ( bAjaxLoading === true ) return;
								var value = url;
								bAjaxLoading = true;
								if ( scope.showNMSList === true && $nms.attr("data-server") === value ) {
									hideNmsLayer();
								} else {
									uncheckAllNms( url );

									$nms.attr("data-server", value);
									$http.get( value ).success( function( result ) {
										$nms.empty();
										if ( angular.isDefined(result.errorCode) ) {
											$nms.html('<h4 style="text-align:center;padding-top:20%;text-decoration:red;text-decoration-color:orange">' + result.errorMessage + '</h4>');
										} else {
											$nms.html( compiledTemplate({ "datum": result }) );
										}
										scope.showNMSList = true;
										bAjaxLoading = false;
									}).error( function() {

									});
								}
								break;
						}
					};
					// for NMS -->
					scope.$on('serverListDirective.initialize', function ( event, oNavbarVoService ) {
						scope.node = null;
						scope.oNavbarVoService = null;
						scope.selectedAgent = "";
						scope.hasScatter = false;
						scope.$broadcast('scatterDirective.initialize.forServerList', oNavbarVoService);
						scope.hideLayer( 0 );
					});
					scope.$on('serverListDirective.show', function ( event, bIsNodeServer, node, oNavbarVoService ) {
						if ( bVisible === true ) {
							scope.hideLayer();
							return;
						}
						bVisible = true;

						if ( angular.isUndefined( scope.node ) || scope.node === null || ( scope.node.key !== node.key ) ) {
							scope.bIsNode = bIsNodeServer;
							scope.node = node;
							scope.oNavbarVoService = oNavbarVoService;
							scope.hasScatter = false;
							if ( bIsNodeServer ) {
								if ( node.isWas ) {
									scope.hasScatter = true;
								}
								scope.serverList = node.serverList;
								scope.bIsNode = true;
								scope.$broadcast('scatterDirective.initializeWithNode.forServerList', node);

								$timeout(function() {
									var instanceName = $element.find( "._node input[type=radio][checked]" ).val();
									showChart( instanceName, scope.node.agentHistogram[instanceName], scope.node.agentTimeSeriesHistogram[instanceName] );
								});
							} else {
								scope.linkList = scope.node.sourceHistogram;
								scope.bIsNode = false;

								$timeout(function () {
									var instanceName = $element.find("._link input[type=radio][checked]").val();
									showChart( instanceName, scope.node.sourceHistogram[instanceName], scope.node.sourceTimeSeriesHistogram[instanceName]);
								});
							}
						}
						showLayer();
					});
				}
			};
		}
	]);
})( jQuery );