'use strict';

pinpointApp.constant('scatterConfig', {
    get: {
        scatterData: '/getScatterData.pinpoint',
        lastScatterData: '/getLastScatterData.pinpoint'
    }
});

// FIXME child window에서 접근할 수 있도록 global변수로 일단 빼둠. 나중에 리팩토링할 것.
//var selectdTracesBox = {};

pinpointApp.directive('scatter',
    [ 'scatterConfig', '$rootScope', '$timeout', 'webStorage', function (cfg, $rootScope, $timeout, webStorage) {
        return {
            template: '<div class="scatter"></div>',
            restrict: 'EA',
            replace: true,
            link: function (scope, element, attrs) {

                // define private variables
                var oScatterChart, oNavbarDao;

                // define private variables of methods
                var showScatter, makeScatter;

                // initialize
                oScatterChart = null;
                oNavbarDao = null;
                scope.popup = [];

                /**
                 * show scatter
                 * @param applicationName
                 * @param from
                 * @param to
                 * @param period
                 * @param filter
                 * @param w
                 * @param h
                 */
                showScatter = function (applicationName, from, to, period, filter, w, h) {
                    if (oScatterChart) {
//							oScatterChart.clear();
                    }

//                    selectdTracesBox = {};
//						var fullscreenButton = $("#scatterChartContainer I.icon-fullscreen");
//						fullscreenButton.data("applicationName", applicationName);
//						fullscreenButton.data("from", from);
//						fullscreenButton.data("to", to);
//						fullscreenButton.data("period", period);
//						fullscreenButton.data("usePeriod", usePeriod);
//						fullscreenButton.data("filter", filter);

//						var downloadButton = $("#scatterChartContainer A");

//						var imageFileName = applicationName +
//								"_" +
//								new Date(from).toString("yyyyMMdd_HHmm") +
//								"~" +
//								new Date(to).toString("yyyyMMdd_HHmm") +
//								"_response_scatter.png";
//
//						downloadButton.attr("download", imageFileName);
//						downloadButton.unbind("click");
//						downloadButton.bind("click", function() {
//							var sImageUrl = oScatterChart.getChartAsPNG();
//							$(this).attr('href', sImageUrl);
//						});

//						$("#scatterChartContainer SPAN").unbind("click");
//						$("#scatterChartContainer SPAN").bind("click", function() {
//							showRequests(applicationName, from, to, period, usePeriod, filter);
//						});

//						$("#scatterChartContainer").show();

                    var bDrawOnceAll = false,
                        nInterval = 2000,
                        fetchLimit = 2000;

                    var htDataSource = {
                        sUrl: function (nFetchIndex) {
//								if (!usePeriod) {
//									return cfg.get.scatterData;
//								}

//								if (nFetchIndex === 0) {
//									return cfg.get.lastScatterData;
//								} else {
                            return cfg.get.scatterData;
//								}
                        },
                        htParam: function (nFetchIndex, htLastFetchParam, htLastFetchedData) {
                            // calculate parameter
                            var htData;
//								console.log("htParam", nFetchIndex, htLastFetchParam, htLastFetchedData);

//								if (nFetchIndex === 0 && !usePeriod) {
                            if (nFetchIndex === 0) {
                                return {
                                    'application': applicationName,
                                    'from': from,
                                    'to': to,
                                    'limit': fetchLimit,
                                    'filter': filter
                                };
                            }

                            // period만큼 먼저 조회해본다.
                            if (nFetchIndex === 0 /*|| typeof(htLastFetchParam) === 'undefined' || typeof(htLastFetchedData) === 'undefined'*/) {
                                htData = {
                                    'application': applicationName,
                                    'period': period,
                                    'limit': fetchLimit,
                                    'filter': filter
                                };
                            } else {
                                if (bDrawOnceAll || htLastFetchedData.scatter.length == 0) {
                                    htData = {
                                        'application': applicationName,
                                        'from': htLastFetchParam.to + 1,
                                        'to': htLastFetchParam.to + 2000,
                                        'limit': fetchLimit,
                                        'filter': filter
                                    };
                                } else {
                                    htData = {
                                        'application': applicationName,
                                        // array[0] 이 최근 값, array[len]이 오래된 이다.
                                        'from': from,
                                        'to': htLastFetchedData.scatter[htLastFetchedData.scatter.length - 1].x - 1,
                                        'limit': fetchLimit,
                                        'filter': filter
                                    };
                                }
                            }

                            return htData;
                        },
                        nFetch: function (htLastFetchParam, htLastFetchedData) {
                            // -1 : stop, n = 0 : immediately, n > 0 : interval
                            var useInterval = false;

//								console.log("nFetch", htLastFetchedData);

                            if (useInterval && htLastFetchedData.scatter.length === 0) {
//									console.log("2A");
                                bDrawOnceAll = true;
                                return nInterval;
                            }

                            if (htLastFetchedData.scatter.length !== 0) {
                                // array[0] 이 최근 값, array[len]이 오래된 이다.
                                if (htLastFetchedData.scatter[0].x > from) {
                                    // TO THE NEXT
                                    return 0;
                                } else {
                                    // STOP
                                    return -1;
                                }
                            }

                            if (htLastFetchedData.scatter[htLastFetchedData.scatter.length - 1] &&
                                htLastFetchedData.scatter[htLastFetchedData.scatter.length - 1].x < date.getTime()) {
                                if (useInterval) {
                                    bDrawOnceAll = true;
                                    return nInterval;
                                }
                                // TO THE NEXT
                                return 0;
                            }

                            // STOP
                            return -1;
                        },
                        htOption: {
                            dataType: 'jsonp',
                            jsonp: '_callback'
                        }
                    };
                    oScatterChart.drawWithDataSource(htDataSource);
                };

                /**
                 * make scatter
                 * @param title
                 * @param start
                 * @param end
                 * @param period
                 * @param w
                 * @param h
                 */
                makeScatter = function (title, start, end, period, w, h) {
                    if (!Modernizr.canvas) {
                        alert("Can't draw scatter. Not supported browser.");
                    }

                    var yAxisMAX = 10000;
//                    var date = new Date();

                    var options = {
                        sContainerId: element,
                        nWidth: w ? w : 400,
                        nHeight: h ? h : 250,
                        // nXMin: date.getTime() - 86400000, nXMax: date.getTime(),
                        nXMin: start, nXMax: end,
                        nYMin: 0, nYMax: yAxisMAX,
                        nZMin: 0, nZMax: 5,
                        nBubbleSize: 3,
                        sXLabel: '',
                        sYLabel: '(ms)',
                        sTitle: title,
                        htTypeAndColor: {
                            // type name : color
                            'Success': '#2ca02c',
                            // 'Warning' : '#f5d025',
                            'Failed': '#d62728'
                        },
                        fOnSelect: function (htPosition, htXY) {
                            var traces = this.getDataByXY(htXY.nXFrom, htXY.nXTo, htXY.nYFrom, htXY.nYTo);
                            if (traces.length === 0) {
                                return;
                            }

                            var token = 'scatterToken_' + _.random(100000, 999999);
                            webStorage.session.add(token, traces);
//                            window.open("/selectedScatter.pinpoint", token);
                            window.open("#/transactionList", token);
                        }
                    };

                    $timeout(function () {
                        if (oScatterChart !== null) {
                            oScatterChart.destroy();
                        }
                        oScatterChart = new BigScatterChart(options);
                        showScatter(title, start, end, period);
                    }, 100);

                };

                scope.$on('scatter.initialize', function (event, navbarDao) {
                    oNavbarDao = navbarDao;
                    makeScatter(oNavbarDao.getApplicationName(), oNavbarDao.getQueryStartTime(), oNavbarDao.getQueryEndTime(), oNavbarDao.getQueryPeriod());
                });
                scope.$on('scatter.initializeWithNode', function (event, node) {
                    makeScatter(node.applicationName || node.text, oNavbarDao.getQueryStartTime(), oNavbarDao.getQueryEndTime(), oNavbarDao.getQueryPeriod());
                });

            }
        };
    } ]);
