(function() {
	'use strict';
	angular.module("pinpointApp").directive("statChartDirective", [
		function () {
			return {
				template: '<div></div>',
				replace: true,
				restrict: 'E',
				scope: {
					namespace: '@' // string value
				},
				link: function postLink(scope, element, attrs) {
					var sId = "", oChart;

					function setIdAutomatically() {
						sId = 'multipleValueAxesId-' + scope.namespace;
						element.attr('id', sId);
					}

					function hasId() {
						return sId === "" ? false : true;
					}

					function setWidthHeight(w, h) {
						if (w) element.css('width', w);
						if (h) element.css('height', h);
					}

					function renderUpdate(data) {
						oChart.dataProvider = data.data;
						oChart.validateData();
					}

					function render(chartData) {
						var options = {
							"type": "serial",
							"theme": "light",
							"autoMargins": false,
							"marginTop": 10,
							"marginLeft": 70,
							"marginRight": 70,
							"marginBottom": 40,
							"legend": {
								"useGraphSettings": true,
								"autoMargins": true,
								"align" : "right",
								"position": "top",
								"valueWidth": 70
							},
							"usePrefixes": true,
							"dataProvider": chartData.data,
							"valueAxes": [
								{
									"id": "v1",
									"gridAlpha": 0,
									"axisAlpha": 1,
									"position": "left",
									"title": "Cpu Usage (%)",
									"maximum" : 100,
									"minimum" : 0
								}
							],
							"graphs": [
								{
									"valueAxis": "v1",
									"balloonText": "[[title]] : [[value]]%",
									"legendValueText": "[[value]]%",
									"lineColor": "#4C0099",
									"fillColor": "#4C0099",
									"lineThickness": 1,
									"title": chartData.title[0],
									"valueField": chartData.field[0],
									"fillAlphas": 0,
									"connect": false
								},
								{
									"valueAxis": "v1",
									"balloonText": "[[title]] : [[value]]%",
									"legendValueText": "[[value]]%",
									"lineColor": "#0000CC",
									"fillColor": "#0000CC",
									"lineThickness": 1,
									"title": chartData.title[1],
									"valueField": chartData.field[1],
									"fillAlphas": 0,
									"connect": false
								},
								{
									"valueAxis": "v1",
									"balloonText": "[[title]] : [[value]]%",
									"legendValueText": "[[value]]%",
									"lineColor": "#66B2FF",
									"fillColor": "#66B2FF",
									"lineThickness": 1,
									"title": chartData.title[2],
									"valueField": chartData.field[2],
									"fillAlphas": 0,
									"connect": false
								}//,
								// {
								// 	"valueAxis": "v1",
								// 	"balloonText": "[[title]] : [[value]]%",
								// 	"legendValueText": "[[value]]%",
								// 	"lineColor": "#0000FF",
								// 	"fillColor": "#0000FF",
								// 	"title": "System(avg)",
								// 	"valueField": "sysAvg",
								// 	"fillAlphas": 0,
								// 	"connect": false
								// },
								// {
								// 	"valueAxis": "v1",
								// 	"balloonText": "[[title]] : [[value]]%",
								// 	"legendValueText": "[[value]]%",
								// 	"lineColor": "#4169E1",
								// 	"fillColor": "#4169E1",
								// 	"title": "System(min)",
								// 	"valueField": "sysMin",
								// 	"fillAlphas": 0,
								// 	"connect": false
								// },
								// {
								// 	"valueAxis": "v1",
								// 	"balloonText": "[[title]] : [[value]]%",
								// 	"legendValueText": "[[value]]%",
								// 	"lineColor": "#87CEEB",
								// 	"fillColor": "#87CEEB",
								// 	"title": "System(Max)",
								// 	"valueField": "sysMax",
								// 	"fillAlphas": 0,
								// 	"connect": false
								// }
							],
							"categoryField": "time",
							"categoryAxis": {
								"axisColor": "#DADADA",
								"startOnAxis": true,
								"gridPosition": "start",
								"labelFunction": function (valueText) {
									return valueText.replace(/\s/, "<br>").replace(/-/g, ".").substring(2);
								}
							}
						};
						oChart = AmCharts.makeChart(sId, options);
						var oChartCursor = new AmCharts.ChartCursor({
							"categoryBalloonAlpha": 0.7,
							"fullWidth": true,
							"cursorAlpha": 0.1
						});
						oChartCursor.addListener('changed', function (event) {
							scope.$emit('statChartDirective.cursorChanged.' + scope.namespace, event);
						});
						oChart.addChartCursor( oChartCursor );
					}

					function showCursorAt(category) {
						if (category) {
							if (angular.isNumber(category)) {
								category = oChart.dataProvider[category].time;
							}
							oChart.chartCursor.showCursorAt(category);
						} else {
							oChart.chartCursor.hideCursor();
						}
					}

					function resize() {
						if (oChart) {
							oChart.validateNow();
							oChart.validateSize();
						}
					}

					scope.$on("statChartDirective.initAndRenderWithData." + scope.namespace, function (event, data, w, h) {
						if ( hasId() ) {
							renderUpdate( data );
						} else {
							setIdAutomatically();
							setWidthHeight(w, h);
							render(data);
						}
					});

					// scope.$on('statChartDirective.showCursorAt.' + scope.namespace, function (event, category) {
					// 	showCursorAt(category);
					// });

					scope.$on('statChartDirective.resize.' + scope.namespace, function (event) {
						resize();
					});
				}
			};
		}
	]);
})();