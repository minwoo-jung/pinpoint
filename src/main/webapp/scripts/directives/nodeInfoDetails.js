'use strict';

pinpointApp.constant('nodeInfoDetailsConfig', {
    applicationStatisticsUrl: '/applicationStatistics.pinpoint',
    myColors: ["#008000", "#4B72E3", "#A74EA7", "#BB5004", "#FF0000"]
});

pinpointApp
    .directive('nodeInfoDetails', [ 'nodeInfoDetailsConfig', function (config) {
        return {
            restrict: 'EA',
            replace: true,
            templateUrl: 'views/nodeInfoDetails.html',
            link: function postLink(scope, element, attrs) {

                // define private variables
                var htServermapData;

                // define private variables of methods
                var reset, showDetailInformation, renderApplicationStatistics, recalculateHistogram;

                /**
                 * reset
                 */
                reset = function () {
                    scope.nodeName = null;
                    scope.nodeCategory = null;
                    scope.nodeIcon = 'USER';
                    scope.unknownGroup = null;
                    scope.hosts = null;
                    scope.showServers = false;
                    scope.agents = null;
                    scope.showAgents = false;
                    scope.showNodeInfoBarChart = false;
                    scope.$digest();
                };

                /**
                 * show detail information
                 * @param query
                 * @param node
                 */
                showDetailInformation = function (query, node) {
                    scope.nodeName = node.text;
                    scope.nodeCategory = node.category;
                    if (node.category !== 'UNKNOWN_GROUP') {
                        scope.nodeIcon = node.category; // do not be reset. because it will be like this '.../icons/.png 404 (Not Found)'
                    }
                    scope.unknownGroup = node.textArr;
                    scope.serverList = node.serverList;
                    scope.showServers = _.isEmpty(scope.serverList) ? false : true;
                    // scope.agents = data.agents;
                    // scope.showAgents = (scope.agents.length > 0) ? true : false;
                    scope.$digest();
                };

//                var getApplicationStatisticsData = function (query, callback) {
//                    jQuery.ajax({
//                        type : 'GET',
//                        url : config.applicationStatisticsUrl,
//                        cache : false,
//                        dataType: 'json',
//                        data : {
//                            from : query.from,
//                            to : query.to,
//                            applicationName : query.applicationName,
//                            serviceType : query.serviceType
//                        },
//                        success : function (result) {
//                            callback(query, result);
//                        },
//                        error : function (xhr, status, error) {
//                            console.log("ERROR", status, error);
//                        }
//                    });
//                };
                /**
                 * render application statistics
                 * @param data
                 */
                renderApplicationStatistics = function (data) {
                    scope.showNodeInfoBarChart = true;
                    scope.$digest();
                    nv.dev = false;
                    nv.addGraph(function () {
                        var chart = nv.models.discreteBarChart().x(function (d) {
                            return d.label;
                        }).y(function (d) {
                            return d.value;
                        }).staggerLabels(false).tooltips(false).showValues(true);

                        chart.xAxis.tickFormat(function (d) {
                            if (angular.isNumber(d)) {
                                return (d >= 1000) ? d / 1000 + "s" : d + "ms";
                            }
                            return d;
                        });

                        chart.yAxis.tickFormat(function (d) {
                            return d;
                        });

                        chart.valueFormat(function (d) {
                            return d;
                        });

                        chart.color(config.myColors);

                        d3.select('.nodeInfoDetails .infoBarChart svg')
                            .datum(data)
                            .transition()
                            .duration(0)
                            .call(chart);

                        nv.utils.windowResize(chart.update);

                        return chart;
                    });
                };

//                var showApplicationStatisticsSummary = function (begin, end, applicationName, serviceType) {
//                    var params = {
//                        "from" : begin,
//                        "to" : end,
//                        "applicationName" : applicationName,
//                        "serviceType" : serviceType
//                    };
//                    getApplicationStatisticsData(params, function (query, result) {
//                        console.log('result', result);
//                        renderApplicationStatistics(result.histogramSummary);
//                    });
//                };


                // histogram 데이터 서버에서 만들지 않고, link정보에서 수집한다.
//                var extractHistogramFromData = function (data) {
//                    var histogram = [];
//                    if (data && data.serverList /*&& angular.isArray(data.serverList) && data.serverList.length > 0*/) {
//                        angular.forEach(data.serverList, function (serverInfo, serverName) {
//                            var i = 0;
//                            angular.forEach(serverInfo.instanceList, function (innerVal, innerKey) {
//                            	if (innerVal.histogram == null) {
//                            		return;
//                            	}
//                            	angular.forEach(innerVal.histogram, function(v, k) {
//                            		if (histogram[i]) {
//                            			histogram[i].value += Number(v, 10);
//                            		} else {
//                            			histogram[i] = {
//                            					'label' : k,
//                            					'value' : Number(v, 10)
//                            			};
//                            		}
//                            		i++;
//                            	});
//                            	i = 0;
//                            });
//                        });
//                    }
//                    var histogramData = [{
//                        'key' : "Response Time Histogram",
//                        'values': histogram
//                    }];
//                    return histogramData;
//                };

                /**
                 * recalculate histogram
                 * @param key
                 * @param linkDataArray
                 * @returns {Array}
                 */
                recalculateHistogram = function (key, linkDataArray) {
                    // application histogram data 서버에서 만들지 않고 클라이언트에서 만든다.
                    // var histogramData = extractHistogramFromData(node);
                    var histogram = [];
                    angular.forEach(linkDataArray, function (value, index) {
                        var i = 0;
                        if (value.to === key) {
                            angular.forEach(value.histogram, function (v, k) {
                                if (histogram[i]) {
                                    histogram[i].value += Number(v, 10);
                                } else {
                                    histogram[i] = {
                                        'label': k,
                                        'value': Number(v, 10)
                                    };
                                }
                                i += 1;
                            });
                        }
                    });
                    return histogram;
                };

                /**
                 * scope event on nodeInfoDetails.initializeWithNodeData
                 */
                scope.$on('nodeInfoDetails.initializeWithNodeData', function (event, e, query, node, mapData) {
                    reset();
                    scope.node = node;
                    htServermapData = mapData;
                    if (!node.rawdata && node.category !== "USER" && node.category !== "UNKNOWN_GROUP") {
//                        showApplicationStatisticsSummary(query.from, query.to, data.text, data.serviceTypeCode);
                        renderApplicationStatistics([
                            {
                                'key': "Response Time Histogram",
                                'values': recalculateHistogram(node.key, mapData.applicationMapData.linkDataArray)
                            }
                        ]);
                        //renderApplicationStatistics(histogramData);
                    }
                    showDetailInformation(query, node);
                });

                /**
                 * scope event on nodeInfoDetails.initializeWithLinkData
                 */
                scope.$on('nodeInfoDetails.initializeWithLinkData', function (event, e, query, link) {
                    reset();
                });


            }
        };
    }]);
