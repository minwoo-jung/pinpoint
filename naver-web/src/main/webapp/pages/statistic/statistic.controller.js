(function() {
	'use strict';
	pinpointApp.constant( "StatisticCtrlConfig", {
		ID: "STAT_CTRL_",
		PAGE_NAME: "statistic",
		SLASH: "/"
	});
	pinpointApp.controller("StatisticCtrl", [ "StatisticCtrlConfig", "$scope", "$routeParams", "locationService", "$http", "CommonUtilService", "AgentDaoService", "UrlVoService",
		function ( cfg, $scope, $routeParams, locationService, $http, CommonUtilService, AgentDaoService, UrlVoService) {
			cfg.ID +=  CommonUtilService.getRandomNum();
			// AnalyticsService.send(AnalyticsService.CONST.INSPECTOR_PAGE);

			$scope.$on( "up.changed.application", function ( event, invokerId, newAppName ) {
				UrlVoService.setApplication( newAppName );
				// UrlVoService.setAgentId( "" );
				changeLocation(function() {
					$scope.$broadcast( "down.changed.application", invokerId );
					loadStatChart();
				});
			});
			$scope.$on( "up.changed.period", function ( event, invokerId ) {
				changeLocation(function() {
					$scope.$broadcast( "down.changed.period", true, invokerId );
					loadStatChart();
				});

			});

			function loadStatChart() {
				var oParam = {
					applicationId: UrlVoService.getApplicationName(),
					from : UrlVoService.getQueryStartTime(),
					to : UrlVoService.getQueryEndTime(),
					sampleRate: 1
				};
				if ( oParam.from > 0 && oParam.to > 0 ) {
					$http.get( "getApplicationStat/cpuLoad/chart.pinpoint" +  getQueryStr(oParam) )
					.then(function(chartData) {
						// var cpuLoad = { id: 'cpuLoad', title: 'JVM/System Cpu Usage', isAvailable: false};

						$scope.$broadcast( "statChartDirective.initAndRenderWithData.jvm", (function(cpuLoadData, agentStatData) {
							var jvmCpuLoadData = agentStatData.charts['CPU_LOAD_JVM'];
							if (jvmCpuLoadData) {
								cpuLoadData.isAvailable = true;
							} else {
								return;
							}
							var newData = [],
								pointsJvmCpuLoad = jvmCpuLoadData.points;

							// if (pointsJvmCpuLoad.length !== pointsSystemCpuLoad.length) {
							// 	throw new Error('assertion error', 'jvmCpuLoad.length != systemCpuLoad.length');
							// }

							for (var i = 0; i < pointsJvmCpuLoad.length; ++i) {
								// if (pointsJvmCpuLoad[i].xVal !== pointsSystemCpuLoad[i].xVal) {
								// 	throw new Error('assertion error', 'timestamp mismatch between jvmCpuLoad and systemCpuLoad');
								// }
								var thisData = {
									"time": moment(pointsJvmCpuLoad[i].xVal).format( "YYYY-MM-DD HH:mm:ss" ),
									"jvmAvg": pointsJvmCpuLoad[i].avgYVal.toFixed(2),
									"jvmMin": pointsJvmCpuLoad[i].minYVal.toFixed(2),
									"jvmMax": pointsJvmCpuLoad[i].maxYVal.toFixed(2)
								};
								newData.push(thisData);
							}
							return {
								title: ["JVM(avg)", "JVM(max)", "JVM(min)"],
								field: ["jvmAvg", "jvmMax", "jvmMin"],
								data: newData
							};

						})({ id: 'jvmCpuLoad', title: 'JVM Cpu Usage', isAvailable: false}, chartData.data), "100%", "270px" );

						$scope.$broadcast( "statChartDirective.initAndRenderWithData.system", (function(cpuLoadData, agentStatData) {
							var systemCpuLoadData = agentStatData.charts['CPU_LOAD_SYSTEM'];
							if (systemCpuLoadData) {
								cpuLoadData.isAvailable = true;
							} else {
								return;
							}
							var newData = [],
								pointsSystemCpuLoad = systemCpuLoadData.points;

							// if (pointsJvmCpuLoad.length !== pointsSystemCpuLoad.length) {
							// 	throw new Error('assertion error', 'jvmCpuLoad.length != systemCpuLoad.length');
							// }

							for (var i = 0; i < pointsSystemCpuLoad.length; ++i) {
								// if (pointsJvmCpuLoad[i].xVal !== pointsSystemCpuLoad[i].xVal) {
								// 	throw new Error('assertion error', 'timestamp mismatch between jvmCpuLoad and systemCpuLoad');
								// }
								var thisData = {
									"time": moment(pointsSystemCpuLoad[i].xVal).format( "YYYY-MM-DD HH:mm:ss" ),
									"sysAvg": pointsSystemCpuLoad[i].avgYVal.toFixed(2),
									"sysMin": pointsSystemCpuLoad[i].minYVal.toFixed(2),
									"sysMax": pointsSystemCpuLoad[i].maxYVal.toFixed(2)
								};
								newData.push(thisData);
							}
							return {
								title: ["System(avg)", "System(max)", "System(min)"],
								field: ["sysAvg", "sysMax", "sysMin"],
								data: newData
							};

						})({ id: 'systemCpuLoad', title: 'System Cpu Usage', isAvailable: false}, chartData.data), "100%", "270px" );
					}, function(error) {

					});
				}
			}
			function changeLocation( callback ) {
				var newPath = getLocation();
				if ( locationService.path() !== newPath ) {
					locationService.skipReload().path( newPath ).replace();
					if (!$scope.$$phase) {
						$scope.$apply();
					}
					callback();
				}
			}
			function getLocation() {
				var url = [
					"",
					cfg.PAGE_NAME,
					UrlVoService.getApplication()
				].join( cfg.SLASH );

				if ( UrlVoService.getReadablePeriod() ) {
					url += cfg.SLASH + UrlVoService.getReadablePeriod() + cfg.SLASH + UrlVoService.getQueryEndDateTime();
				}
				return url;
			}
			function getQueryStr( o ) {
				var query = "?";
				for( var p in o ) {
					query += ( query == "?" ? "" : "&" ) + p + "=" + o[p];
				}
				return query;
			}

			$scope.$broadcast("down.initialize", cfg.ID );
			loadStatChart();
		}
	]);
})();