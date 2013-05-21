var oServerMap = null;
var FILTER_DELIMETER = "^";
var FILTER_ENTRY_DELIMETER = "|";

function linkStatistics(
		begin,
		end,
		srcServiceType,
		srcApplicationName,
		destServiceType,
		destApplicationName) {
	
	var params = {
			"from" : begin,
			"to" : end,
			"srcServiceType" : srcServiceType,
			"srcApplicationName" : srcApplicationName,
			"destServiceType" : destServiceType,
			"destApplicationName" : destApplicationName
	}

	var showFailedRateChart = function(data) {
		$("#linkInfoSFChart").show();
		nv.addGraph(function() {
			var chart = nv.models.stackedAreaChart().x(function(d) {
				return d[0];
			}).y(function(d) {
				return d[1];
			}).clipEdge(true).color(function(d) {
				if (d.key == "Success") {
					return "green";
				} else if (d.key == "Failed") {
					return "red";
				} else {
					return nv.utils.getColor(d);
				}
			}).style('expand').showControls(false);

			chart.xAxis.tickFormat(function(d) {
				return d3.time.format('%x')(new Date(d));
			});

			chart.yAxis.tickFormat(d3.format(',.2f'));

			d3.select('#linkInfoSFChart svg')
				.datum(data)
				.transition()
				.duration(500)
				.call(chart);

			nv.utils.windowResize(chart.update);

			return chart;
		});
	};
	
	var showSummary = function(data) {
		$("#linkInfoBarChart").show();
		nv.addGraph(function() {
			var chart = nv.models.discreteBarChart().x(function(d) {
				return d.label
			}).y(function(d) {
				return d.value
			}).staggerLabels(true).tooltips(false).showValues(true)
	
			d3.select('#linkInfoBarChart svg')
					.datum(data)
					.transition()
					.duration(500)
					.call(chart);
	
			nv.utils.windowResize(chart.update);
	
			return chart;
		});
	}

	var showTimeseriesHistogram = function(data) {
		$("#linkInfoChart").show();
		nv.addGraph(function() {
			var chart = nv.models.stackedAreaChart().x(function(d) {
				return d[0];
			}).y(function(d) {
				return d[1];
			}).clipEdge(true);
			
			chart.xAxis.tickFormat(function(d) {
				return d3.time.format('%x')(new Date(d));
			});
			
			chart.yAxis.tickFormat(d3.format(',.2f'));
			
			d3.select('#linkInfoChart svg')
			.datum(data)
			.transition()
			.duration(500)
			.call(chart);
			
			nv.utils.windowResize(chart.update);
			
			return chart;
		});
	}; 
	
	getLinkStatisticsData(params, function(query, result) {
		showFailedRateChart(result.timeseriesFailRate);
		showSummary(result.histogramSummary);
		//showTimeseriesHistogram(result.)
	});
}

function filteredLinkStatistics(
		applicationName,
		serviceType,
		begin,
		end,
		srcServiceType,
		srcApplicationName,
		destServiceType,
		destApplicationName,
		prevFilter) {
	
	var params = {
			"application" : applicationName,
			"serviceType" : serviceType,
			"from" : begin,
			"to" : end,
			"srcServiceType" : srcServiceType,
			"srcApplicationName" : srcApplicationName,
			"destServiceType" : destServiceType,
			"destApplicationName" : destApplicationName,
			"filter" : prevFilter
	}
	window.open("/filteredLinkStatistics.pinpoint?" + decodeURIComponent($.param(params)), "");
}

function filterPassingTransaction(
			applicationName,
			serviceType,
			begin,
			end,
			srcServiceType,
			srcApplicationName,
			destServiceType,
			destApplicationName,
			prevFilter) {
	
	var params = {
		"application" : applicationName,
		"serviceType" : serviceType,
		"from" : begin,
		"to" : end,
		"filter" : ((prevFilter) ? prevFilter + FILTER_DELIMETER : "")
					+ srcServiceType + FILTER_ENTRY_DELIMETER
					+ srcApplicationName + FILTER_ENTRY_DELIMETER
					+ destServiceType + FILTER_ENTRY_DELIMETER
					+ destApplicationName
	}
	window.open("/filtermap.pinpoint?" + decodeURIComponent($.param(params)), "");
}

function getLinkStatisticsData(query, callback) {
    jQuery.ajax({
    	type : 'GET',
    	url : '/linkStatistics.pinpoint',
    	cache : false,
    	dataType: 'json',
    	data : {
			from : query.from,
			to : query.to,
			srcServiceType : query.srcServiceType,
			srcApplicationName : query.srcApplicationName,
			destServiceType : query.destServiceType,
			destApplicationName : query.destApplicationName
    	},
    	success : function(result) {
    		callback(query, result);
    	},
    	error : function(xhr, status, error) {
    		console.log("ERROR", status, error);
    	}
    });
}

function getServerMapData2(query, callback) {
    jQuery.ajax({
    	type : 'GET',
    	url : '/getServerMapData2.pinpoint',
    	cache : false,
    	dataType: 'json',
    	data : {
    		application : query.applicationName,
    		serviceType : query.serviceType,
    		from : query.from,
    		to : query.to
    	},
    	success : function(result) {
    		callback(query, result);
    	},
    	error : function(xhr, status, error) {
    		console.log("ERROR", status, error);
    	}
    });
}

function getLastServerMapData2(query, callback) {
    jQuery.ajax({
    	type : 'GET',
    	url : '/getLastServerMapData2.pinpoint',
    	cache : false,
    	dataType: 'json',
    	data : {
    		application : query.applicationName,
    		serviceType : query.serviceType,
    		period : query.period
    	},
    	success : function(result) {
    		callback(query, result);
    	},
    	error : function(xhr, status, error) {
    		console.log("ERROR", status, error);
    	}
    });
}

function getFilteredServerMapData(query, callback) {
    jQuery.ajax({
    	type : 'GET',
    	url : '/getFilteredServerMapData.pinpoint',
    	cache : false,
    	dataType: 'json',
    	data : {
    		application : query.applicationName,
    		serviceType : query.serviceType,
    		from : query.from,
    		to : query.to,
    		filter : query.filter
    	},
    	success : function(result) {
    		callback(query, result);
    	},
    	error : function(xhr, status, error) {
    		console.log("ERROR", status, error);
    	}
    });
}

var serverMapCachedData;
var serverMapCachedQuery;

function toggleMerge() {
	if (serverMapCachedData && serverMapCachedQuery) {
		$(".nodeinfo").remove();
		$(".linkinfo").remove();
		if (oServerMap) {
			oServerMap.clear();
		}
		serverMapCallback(serverMapCachedQuery, serverMapCachedData, true);
	}
}

var serverMapCallback = function(query, data, ignoreCache) {
	var containerId = "servermap";
	
	var cloneObject = function(obj) {
	    var newObj = (obj instanceof Array) ? [] : {};
	    for (var i in obj) {
	        if (obj[i] && typeof obj[i] == "object") {
	            newObj[i] = cloneObject(obj[i]);
	        } else {
	            newObj[i] = obj[i];
	        }
	    }
	    return newObj;
	}; 
	
	serverMapCachedQuery = cloneObject(query);
	serverMapCachedData = cloneObject(data);
	
	if (data.applicationMapData.nodeDataArray.length == 0) {
		warning("NO DATA", "");
		return;
	} else {
		clearAllWarnings();
		$("#" + containerId).show();
	}

	if ($('#mergeUnknown').is(':checked')) {
		mergeUnknown(data);
	}

	replaceClientToUser(data);

	if (oServerMap == null) {
		oServerMap = new ServerMap({
	        sContainerId : containerId,
			"sImageDir" : '/images/icons/',
			"htIcons" : {
				'APACHE' : 'APACHE.png',
				'ARCUS' : 'ARCUS.png',
				'CUBRID' : 'CUBRID.png',
				'ETC' : 'ETC.png',
				'MEMCACHED' : 'MEMCACHED.png',
				'MYSQL' : 'MYSQL.png',
				'QUEUE' : 'QUEUE.png',
				'TOMCAT' : 'TOMCAT.png',
				'UNKNOWN_CLOUD' : 'UNKNOWN_CLOUD.png',
				'UNKNOWN_GROUP' : 'UNKNOWN_CLOUD.png',
				'USER' : 'USER.png'
			},
			fOnNodeContextClick : function(e, data) {
				nodeContextClickHandler(e, query, data, "#" + containerId);
			},
			fOnLinkContextClick : function(e, data) {
				linkContextClickHandler(e, query, data, "#" + containerId);
			},
			fOnLinkClick : function(e, data) {
				linkClickHandler(e, query, data, "#" + containerId);
			}
	    });
	} else {
		oServerMap.option({
			fOnNodeContextClick : function(e, data) {
				nodeContextClickHandler(e, query, data, "#" + containerId);
			},
			fOnLinkContextClick : function(e, data) {
				linkContextClickHandler(e, query, data, "#" + containerId);
			},
			fOnLinkClick : function(e, data) {
				linkClickHandler(e, query, data, "#" + containerId);
			}
		});
	}
    oServerMap.load(data.applicationMapData);
};

function showServerMap(applicationName, serviceType, from, to, period, usePeriod, filterText, cb) {
	console.log("showServerMap", applicationName, serviceType, from, to, period, usePeriod, filterText, cb);
	
	$(".nodeinfo").remove();
	$(".linkinfo").remove();

	if (oServerMap) {
		oServerMap.clear();
	}
	
	var query = {
		applicationName : applicationName,
		serviceType : serviceType, 
		from : from,
		to : to, 
		period : period,
		usePeriod : usePeriod,
		filter : filterText
	};

	console.log("filterText", filterText);
    
    if (filterText) {
    	getFilteredServerMapData(query, function(query, result) {
	    		if (cb) {
	    			cb(query, result);
	    		}
	    		serverMapCallback(query, result);
    		});
    } else if (usePeriod) {
        getLastServerMapData2(query, function(query, result) {
    		if (cb) {
    			cb(query, result);
    		}
    		serverMapCallback(query, result);
		});
    } else {
        getServerMapData2(query, function(query, result) {
    		if (cb) {
    			cb(query, result);
    		}
    		serverMapCallback(query, result);
		});
    }
}

// TODO 임시코드로 나중에 USER와 backend를 구분할 예정.
var replaceClientToUser = function(data) {
	var nodes = data.applicationMapData.nodeDataArray;
	nodes.forEach(function(node) {
		if (node.category == "CLIENT") {
			node.category = "USER";
			node.text = "USER";
		}
	});
}

var mergeUnknown = function(data) {
	var nodes = data.applicationMapData.nodeDataArray;
	var links = data.applicationMapData.linkDataArray;
	
	var inboundCountMap = {};
	nodes.forEach(function(node) {
		if (!inboundCountMap[node.key]) {
			inboundCountMap[node.key] = {
				"sourceCount" : 0,
				"totalCallCount" : 0
			};
		}
		
		links.forEach(function(link) {
			if (link.to == node.key) {
				inboundCountMap[node.key].sourceCount++;
				inboundCountMap[node.key].totalCallCount += link.text;
			}
		});
	});
	
	var newNodeList = [];
	var newLinkList = [];
	
	var removeNodeIdSet = {};
	var removeLinkIdSet = {};
	
	nodes.forEach(function(node, nodeIndex) {
		if (node.category == "UNKNOWN_CLOUD") {
			return;
		}
		
		var newNode;
		var newLink;
		var newNodeKey = "UNKNOWN_GROUP_" + node.key;

		var unknownCount = 0;
		links.forEach(function(link, linkIndex) {
			if (link.from == node.key &&
				link.targetinfo.serviceType == "UNKNOWN_CLOUD" &&
				inboundCountMap[link.to] && inboundCountMap[link.to].sourceCount == 1) {
				unknownCount++;
			}
		});
		if (unknownCount < 2) {
			return;
		}
		
		// for each children.
		links.forEach(function(link, linkIndex) {
			if (link.targetinfo.serviceType != "UNKNOWN_CLOUD") {
				return;
			}
			if (inboundCountMap[link.to] && inboundCountMap[link.to].sourceCount > 1) {
				return;
			}

			// branch out from current node.
			if (link.from == node.key) {
				if (!newNode) {
					newNode = {
				    	"id" : newNodeKey,
				    	"key" : newNodeKey,
					    "text" : "",
					    "hosts" : [],
					    "category" : "UNKNOWN_GROUP",
					    "terminal" : "true",
					    "agents" : [],
					    "fig" : "FramedRectangle"
					}
				}
				if (!newLink) {
					newLink = {
					    	"id" : node.key + "-" + newNodeKey,
							"from" : node.key,
							"to" : newNodeKey,
							"sourceinfo" : [],
							"targetinfo" : [],
							"text" : 0,
							"error" : 0,
							"slow" : 0,
							"rawhistogram" : [],
							"histogram" : {}
					};
				}
				
				// fill the new node/link informations.
				newNode.text += link.targetinfo.applicationName + " (" + link.text + ")\n";

				newLink.text += link.text;
				newLink.error += link.error;
				newLink.slow += link.slow;
				newLink.sourceinfo.push(link.sourceinfo);
				newLink.targetinfo.push(link.targetinfo);
				newLink.rawhistogram.push(link.histogram);

				$.each(link.histogram, function(key, value) {
					if (newLink.histogram[key]) {
						newLink.histogram[key] += value;	
					} else {
						newLink.histogram[key] = value;
					}
				});
				
				removeNodeIdSet[link.to] = null;
				removeLinkIdSet[link.id] = null;
			}
		});
		
		if (newNode) {
			newNodeList.push(newNode);
		}
		
		if (newLink) {
			newLinkList.push(newLink);
		}
	});
	
	newNodeList.forEach(function(newNode) {
		data.applicationMapData.nodeDataArray.push(newNode);
	});
	
	newLinkList.forEach(function(newLink) {
		data.applicationMapData.linkDataArray.push(newLink);
	});
	
	$.each(removeNodeIdSet, function(key, val) {
		nodes.forEach(function(node, i) {
			if (node.id == key) {
				nodes.splice(i, 1);
			}
		});
	});
	
	$.each(removeLinkIdSet, function(key, val) {
		links.forEach(function(link, i) {
			if (link.id == key) {
				links.splice(i, 1);
			}
		});			
	});
}

var nodeContextClickHandler = function(e, query, data, containerId) {
	if ($("DIV.nodeinfo" + data.id).length > 0) {
		$("DIV.nodeinfo" + data.id).remove();
		return;
	}
	
	data.query = query;
	var htOffset = $(containerId).offset();
	var template;
	if (data.category == "CLIENT") {
		template = $('#ClientContextInfoBox');
	} else if (data.category == "UNKNOWN_GROUP") {
		template = $('#UnknownGroupContextInfoBox');
	} else {
		template = $('#ApplicationContextInfoBox');
	}
	
	var box = template
				.tmpl(data)
				.css({'top':e.pageY - htOffset.top, 'left':e.pageX - htOffset.left, 'z-index':300})
				.addClass('nodeinfo')
				.addClass('nodeinfo' + data.id);
	
	box.appendTo($(containerId).parent());
}

var linkContextClickHandler = function(e, query, data, containerId) {
	if ($("DIV.linkinfo" + data.id).length > 0) {
		$("DIV.linkinfo" + data.id).remove();
		return;
	}
	data.query = query;
	var htOffset = $(containerId).offset();
	var box = $('#LinkContextInfoBox')
				.tmpl(data)
				.css({'top':e.pageY - htOffset.top, 'left':e.pageX - htOffset.left, 'z-index':300})
				.addClass('linkinfo')
				.addClass('linkinfo' + data.id);
	box.appendTo($(containerId).parent());
}

var linkClickHandler = function(e, query, data, containerId) {
	data.query = query;
	$('#linkInfoDetails').empty();
	$('#linkInfoDetails').append($('#LinkInfoBox').tmpl(data));
	
	
	linkStatistics(	query.from,
					query.to,
					data.sourceinfo.serviceTypeCode,
					data.sourceinfo.applicationName,
					data.targetinfo.serviceTypeCode,
					data.targetinfo.applicationName);

	/*
	showLinkHistogramDetailed();
	showLinkHistogramSum();
	showLinkSuccessOrFailedDetailed();
	*/
}