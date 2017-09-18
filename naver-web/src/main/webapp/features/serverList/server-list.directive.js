(function( $ ) {
	'use strict';
	/**
	 * (en)serverListDirective
	 * @ko serverListDirective
	 * @group Directive
	 * @name serverListDirective
	 * @class
	 */
	pinpointApp.directive( "serverListDirective", [ "$http", "$timeout", "$window", "AnalyticsService",
		function ( $http, $timeout, $window, analyticsService ) {
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
					function showLayer() {
						$element.animate({
							"right": 421
						}, 500, function() {});
					}
					function showChart( instanceName, histogram, timeSeriesHistogram ) {
						scope.selectedAgent = instanceName;
						scope.$broadcast('changedCurrentAgent.forServerList', instanceName );
						if ( bInitialized ) {
							scope.$broadcast('responseTimeSummaryChartDirective.updateData.forServerList', histogram);
							scope.$broadcast('loadChartDirective.updateData.forServerList', timeSeriesHistogram);
						} else {
							scope.$broadcast('responseTimeSummaryChartDirective.initAndRenderWithData.forServerList', histogram, '360px', '150px', false, true);
							scope.$broadcast('loadChartDirective.initAndRenderWithData.forServerList', timeSeriesHistogram, '360px', '200px', false, true);
							bInitialized = true;
						}
					}
					function setData(bIsNodeServer, node, serverHistogramData, oNavbarVoService) {
						scope.node = node;
						scope.serverHistogramData = serverHistogramData;
						scope.oNavbarVoService = oNavbarVoService;
						scope.hasScatter = false;
						if ( serverHistogramData ) {
							scope.serverList = serverHistogramData.serverList;
							if (node.isWas) {
								scope.hasScatter = true;
								if (scope.namespace === "forMain") {
									scope.$broadcast('scatterDirective.initializeWithNode.forServerList', node);
								} else {
									scope.$broadcast('scatterDirective.showByNode.forServerList', node);
								}
							}
							$timeout(function () {
								var instanceName = $element.find("._node input[type=radio][checked]").val();
								try {
									showChart(instanceName, serverHistogramData.agentHistogram[instanceName], serverHistogramData.agentTimeSeriesHistogram[instanceName]);
								} catch (e) {}
							});
						} else {
							// 일단 이전 버젼용
							scope.serverList = node.serverList;
							if (node.isWas) {
								scope.hasScatter = true;
								if (scope.namespace === "forMain") {
									scope.$broadcast('scatterDirective.initializeWithNode.forServerList', node);
								} else {
									scope.$broadcast('scatterDirective.showByNode.forServerList', node);
								}
							}
							$timeout(function () {
								var instanceName = $element.find("._node input[type=radio][checked]").val();
								try {
									showChart(instanceName, scope.node.agentHistogram[instanceName], scope.node.agentTimeSeriesHistogram[instanceName]);
								} catch (e) {}
							});
						}
					}
					scope.hideLayer = function( delay ) {
						delay = delay || 100;
						$element.animate({
							"right": -386
						}, delay, function() {
							bVisible = false;
							hideNmsLayer();
						});
					};
					scope.hasError = function( instanceName ) {
						var instance = scope.serverHistogramData.agentHistogram[instanceName];
						return (instance && instance.Error && instance.Error > 0 ) ? "red": "";
					};
					scope.openInspector = function( $event, instanceName ) {
						$event.preventDefault();
						analyticsService.send( analyticsService.CONST.MAIN, analyticsService.CONST.CLK_OPEN_INSPECTOR );
						$window.open( "#/inspector/" + ( scope.node.applicationName || scope.node.filterApplicationName ) + "@" + ( scope.node.serviceType || "" ) + "/" + scope.oNavbarVoService.getReadablePeriod() + "/" + scope.oNavbarVoService.getQueryEndDateTime() + "/" + instanceName );
					};
					scope.selectServer = function( instanceName ) {
						if ( scope.serverHistogramData ) {
							showChart( instanceName, scope.serverHistogramData.agentHistogram[instanceName], scope.serverHistogramData.agentTimeSeriesHistogram[instanceName] );
						} else {
							showChart( instanceName, scope.node.agentHistogram[instanceName], scope.node.agentTimeSeriesHistogram[instanceName] );
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
					scope.$on('serverListDirective.show', function ( event, bIsNodeServer, node, serverHistogramData, oNavbarVoService ) {
						if ( bVisible === true ) {
							scope.hideLayer();
							return;
						}
						bVisible = true;
						if ( angular.isUndefined( scope.node ) || scope.node === null || ( scope.node.key !== node.key ) ) {
							setData(bIsNodeServer, node, serverHistogramData, oNavbarVoService);
						}
						showLayer();
					});
					scope.$on('serverListDirective.setData', function ( event, bIsNodeServer, node, serverHistogramData, oNavbarVoService ) {
						if ( angular.isUndefined( scope.node ) || scope.node === null || ( scope.node.key !== node.key ) ) {
							setData(bIsNodeServer, node, serverHistogramData, oNavbarVoService);
						}
					});
				}
			};
		}
	]);
})( jQuery );