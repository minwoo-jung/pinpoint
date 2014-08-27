'use strict';

pinpointApp.directive('distributedCallFlow', [ '$filter', '$timeout',
    function ($filter, $timeout) {
        return {
            restrict: 'E',
            replace: true,
            templateUrl: 'views/distributedCallFlow.html',
            scope : {
                namespace : '@' // string value
            },
            link: function postLink(scope, element, attrs) {

                // initialize variables
                var grid, columns, dataView, lastAgent;

                // initialize variables of methods
                var initialize, treeFormatter, treeFilter, parseData, execTimeFormatter,
                    getColorByString, progressBarFormatter, argumentFormatter, hasChildNode;

                // bootstrap
                window.callStacks = []; // Slick.Data.DataView 때문에, window 프로퍼티로 사용해야 scope 문제가 해결됨.

                /**
                 * get color by string
                 * @param str
                 * @returns {string}
                 */
                getColorByString = function(str) {
                    // str to hash
                    for (var i = 0, hash = 0; i < str.length; hash = str.charCodeAt(i++) + ((hash << 5) - hash));
                    // int/hash to hex
                    for (var i = 0, colour = "#"; i < 3; colour += ("00" + ((hash >> i++ * 8) & 0xFF).toString(16)).slice(-2));
                    return colour;
                };

                /**
                 * tree formatter
                 * @param row
                 * @param cell
                 * @param value
                 * @param columnDef
                 * @param dataContext
                 * @returns {string}
                 */
                treeFormatter = function (row, cell, value, columnDef, dataContext) {
                    var html = [];

                    value = value.replace(/&/g,"&amp;").replace(/</g,"&lt;").replace(/>/g,"&gt;");
                    var item = dataView.getItemById(dataContext.id);
                    lastAgent = item.agent ? item.agent : lastAgent;

                    var leftBarColor = getColorByString(lastAgent),
                        idx = dataView.getIdxById(dataContext.id),
                        divClass = 'dcf-popover';

                    if (item.hasException) {
                        divClass += ' has-exception';
                    } else if (!item.isMethod) {
                        divClass += ' not-method';
                    }
                    html.push('<div class="'+divClass+'" data-container=".grid-canvas" data-toggle="popover" data-trigger="manual" data-placement="right" data-content="'+value+'">');
                    html.push("<div style='position:absolute;top:0;left:0;bottom:0;width:5px;background-color:"+leftBarColor+"'></div>");
                    html.push("<span style='display:inline-block;height:1px;width:" + (15 * dataContext["indent"]) + "px'></span>");

                    if (window.callStacks[idx + 1] && window.callStacks[idx + 1].indent > window.callStacks[idx].indent) {
                        if (dataContext._collapsed) {
                            html.push(" <span class='toggle expand'></span>&nbsp;");
                        } else {
                            html.push(" <span class='toggle collapse'></span>&nbsp;");
                        }
                    } else {
                        html.push(" <span class='toggle'></span>&nbsp;");
                    }

                    if (item.hasException) {
                        html.push('<span class="glyphicon glyphicon-fire"></span>&nbsp;');
                    } else if (!item.isMethod) {
                        html.push('<span class="glyphicon glyphicon-info-sign"></span>&nbsp;');
                    }

                    html.push(value);
                    html.push('</div>');

                    return html.join('');
                };

                /**
                 * tree filter
                 * @param item
                 * @returns {boolean}
                 */
                treeFilter = function (item) {
                    var result = true;
                    if (item.parent != null) {
                        var parent = window.callStacks[item.parent];
                        while (parent) {

                            if (parent._collapsed) {
                                result = false;
                            }
                            parent = window.callStacks[parent.parent];
                        }
                    }
                    return result;
                };

                /**
                 * argument formatter
                 * @param row
                 * @param cell
                 * @param value
                 * @param columnDef
                 * @param dataConrtext
                 * @returns {string}
                 */
                argumentFormatter = function (row, cell, value, columnDef, dataConrtext) {
                    var html = [];

                    html.push('<div class="dcf-popover" data-container=".grid-canvas" data-toggle="popover" data-trigger="manual" data-placement="right" data-content="'+value+'">');
                    html.push(value);
                    html.push('</div>');
                    return html.join('');
                };

                /**
                 * exec time formatter
                 * @param row
                 * @param cell
                 * @param value
                 * @param columnDef
                 * @param dataContext
                 * @returns {*}
                 */
                execTimeFormatter = function (row, cell, value, columnDef, dataContext) {
                    return $filter('date')(value, 'HH:mm:ss sss');
                };

                /**
                 * progress bar formatter
                 * @param row
                 * @param cell
                 * @param value
                 * @param columnDef
                 * @param dataContext
                 * @returns {string}
                 */
                progressBarFormatter = function (row, cell, value, columnDef, dataContext) {
                    if (value == null || value === "" || value == 0) {
                        return "";
                    }
                    var color;
                    if (value < 30) {
                        color = "#5bc0de";
                    } else if (value < 70) {
                        color = "#5bc0de";
                    } else {
                        color = "#5bc0de";
                    }
                    return "<span class='percent-complete-bar' style='background:" + color + ";width:" + value + "%'></span>";
                };

                // columns
                columns = [
                    {id: "method", name: "Method", field: "method", width: 400, formatter: treeFormatter},
                    {id: "argument", name: "Argument", field: "argument", width: 300, formatter: argumentFormatter},
                    {id: "exec-time", name: "Exec Time", field: "execTime", width: 90, formatter: execTimeFormatter},
                    {id: "gap-ms", name: "Gap(ms)", field: "gapMs", width: 60, cssClass: "right-align"},
                    {id: "time-ms", name: "Time(ms)", field: "timeMs", width: 60, cssClass: "right-align"},
                    {id: "time-per", name: "Time(%)", field: "timePer", width: 100, formatter: progressBarFormatter},
                    {id: "class", name: "Class", field: "class", width: 120},
                    {id: "api-type", name: "Api Type", field: "apiType", width: 90},
                    {id: "agent", name: "Agent", field: "agent", width: 130},
                    {id: "application-name", name: "Application Name", field: "applicationName", width: 150}
                ];

                /**
                 * parse data
                 * @param index
                 * @param callStacks
                 * @returns {Array}
                 */
                parseData = function(index, callStacks) {
                    var result = [],
                        barRatio = 100 / (callStacks[0][index.end] - callStacks[0][index.begin]);
                    angular.forEach(callStacks, function (val, key) {
                        result.push({
                            id: 'id_' + key,
                            parent: val[index['parentId']] ? val[index['parentId']] - 1 : null,
                            indent: val[index['tab']],
                            method: val[index['title']],
                            argument: val[index['arguments']],
                            execTime: val[index['begin']] > 0 ? val[index['begin']] : null,
                            gapMs: val[index['gap']],
                            timeMs: val[index['elapsedTime']],
                            timePer: val[index['elapsedTime']] ? ((val[index['end']] - val[index['begin']]) * barRatio) + 0.9 : null,
                            class: val[index['simpleClassName']],
                            apiType: val[index['apiType']],
                            agent: val[index['agent']],
                            applicationName: val[index['applicationName']],
                            hasException: val[index['hasException']],
                            isMethod: val[index['isMethod']]
                        });
                    });
                    return result;
                };

                /**
                 * initialize
                 * @param t transactionDetail
                 */
                initialize = function (t) {
                    window.callStacks = parseData(t.callStackIndex, t.callStack);
                    // initialize the model

                    var options = {
                        enableCellNavigation: true,
                        enableColumnReorder: true,
                        enableTextSelectionOnCells: true,
//                        autoHeight: true,
                        topPanelHeight: 30,
                        rowHeight: 25
                    };

                    dataView = new Slick.Data.DataView({ inlineFilters: true });
                    dataView.beginUpdate();
                    dataView.setItems(window.callStacks);
                    dataView.setFilter(treeFilter);
//                    dataView.getItemMetadata = function (row) {
//                        var item = dataView.getItemByIdx(row);
//                        if (!item.execTime) {
//                            return {
//                                "columns": {
//                                    2: {
//                                        "colspan": "*"
//                                    }
//                                }
//                            };
//                        }
//                    };
                    dataView.endUpdate();

                    grid = new Slick.Grid(element.get(0), dataView, columns, options);

                    var isSingleClick = true, clickTimeout = false;
                    grid.onClick.subscribe(function (e, args) {
                        if ($(e.target).hasClass("toggle")) {
                            var item = dataView.getItem(args.row);
                            if (item) {
                                if (!item._collapsed) {
                                    item._collapsed = true;
                                } else {
                                    item._collapsed = false;
                                }
                                dataView.updateItem(item.id, item);
                            }
                            e.stopImmediatePropagation();
                        }

                        if (!clickTimeout) {
                            clickTimeout = $timeout(function () {
                                if (isSingleClick) {
                                    element.find('.dcf-popover').popover('hide');
                                }
                                isSingleClick = true;
                                clickTimeout = false;
                            }, 300);
                        }
                    });

                    grid.onDblClick.subscribe(function (e, args) {
                        isSingleClick = false;
//                        console.log('isSingleClick = false');
                        $(e.target).popover('toggle');
                    });

                    grid.onCellChange.subscribe(function (e, args) {
                        dataView.updateItem(args.item.id, args.item);
                    });

                    grid.onActiveCellChanged.subscribe(function (e, args) {
                        scope.$emit('distributedCallFlow.rowSelected.' + scope.namespace, args.grid.getDataItem(args.row));
                    });

                    hasChildNode = function (row) {
                        var nextItem = dataView.getItem(row + 1);
                        if (nextItem) {
                            if (row === nextItem.parent) {
                                return true;
                            }
                        }
                        return false;
                    };

                    grid.onKeyDown.subscribe(function (e, args) {
                        var item = dataView.getItem(args.row);

                        if (e.which == 37) { // left
                            if (hasChildNode(args.row)) {
                                item._collapsed = true;
                                dataView.updateItem(item.id, item);
//                            } else if (item.indent > 0 && item.parent >= 0) {
//                                var parent = dataView.getItem(item.parent);
//                                parent._collapsed = true;
//                                dataView.updateItem(item.id, item);
//                                grid.setActiveCell(dataView.getRowById(parent.id), 0);
                            } else {
                                var prevItem = dataView.getItem(args.row - 1);
                                if (prevItem) {
                                    grid.setActiveCell(args.row - 1, 0);
                                }
                            }
                        } else if (e.which == 39) { // right
                            if (item._collapsed) {
                                item._collapsed = false;
                            } else {
                                var nextItem = dataView.getItem(args.row + 1);
                                if (nextItem) {
                                    grid.setActiveCell(args.row + 1, 0);
                                }
                            }
                            dataView.updateItem(item.id, item);
                        }
                    });

                    // wire up model events to drive the grid
                    dataView.onRowCountChanged.subscribe(function (e, args) {
                        grid.updateRowCount();
                        grid.render();
                    });

                    dataView.onRowsChanged.subscribe(function (e, args) {
                        grid.invalidateRows(args.rows);
                        grid.render();
                    });

                };

                /**
                 * scope event on distributedCallFlow.initialize
                 */
                scope.$on('distributedCallFlow.initialize.' + scope.namespace, function (event, transactionDetail) {
                    initialize(transactionDetail);
                });

                /**
                 * scope event on distributedCallFlow.resize
                 */
                scope.$on('distributedCallFlow.resize.' + scope.namespace, function (event) {
                    grid.resizeCanvas();
                });
            }
        };
    }
]);
