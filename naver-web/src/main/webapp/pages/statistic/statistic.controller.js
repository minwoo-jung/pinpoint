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
					agentId: "FrontWAS2",
					from : UrlVoService.getQueryStartTime(),
					to : UrlVoService.getQueryEndTime(),
					sampleRate: 1
				};
				if ( oParam.from > 0 && oParam.to > 0 ) {
					$http.get( "/getAgentStat/cpuLoad/chart.pinpoint" +  getQueryStr(oParam) )
					.then(function(chartData) {
						var cpuLoad = { id: 'cpuLoad', title: 'JVM/System Cpu Usage', isAvailable: false};

						$scope.$broadcast( "statChartDirective.initAndRenderWithData", AgentDaoService.parseCpuLoadChartDataForAmcharts(cpuLoad, chartData.data), "100%", "270px" );
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