(function() {
	'use strict';
	/**
	 * (en)serverListDirective 
	 * @ko serverListDirective
	 * @group Directive
	 * @name serverListDirective
	 * @class
	 */
	pinpointApp.directive('serverListDirective', [ '$timeout', '$window', '$filter', 'helpContentTemplate', 'helpContentService', 'SQLAjaxService', 
	    function ($timeout, $window, $filter, helpContentTemplate, helpContentService, ajaxService) {
            return {
                restrict: 'A',
                link: function postLink(scope, element) {
                	var bInitialized = false;
                	var bIsNode = false;
                	var $element = jQuery(element);
                	var $nms = jQuery($element.find(".nms"));
                	var bAjaxLoading = false;
                	var showModal = function() {
                		$element.modal({});
                	};
                	$element.on('show.bs.modal', function() {
                		var $$window = jQuery($window);
                		var sidebarWidth = 422;
                		var windowWidth = $$window.width();
                		var modalWidth = $element.find(".modal-dialog").width();
                		var mainWidth = windowWidth - sidebarWidth;
                		var sideWidth = (windowWidth - modalWidth) / 2;
                		if ( mainWidth >= modalWidth ) {
                			$element.css("left", -( sidebarWidth - sideWidth + (mainWidth - modalWidth) / 2) * 2 + "px" );
                		}
                		$element.find(".modal-body").css("height", $$window.height() * 0.7 );
                		$element.find(".server-wrapper").css("height", $$window.height() * 0.7 - 70 );
                		$element.find(".nms-list").css("height", $$window.height() * 0.7 - 70 );
                	});
                	$element.on('hide.bs.modal', function() {
                		scope.showNMSList = false;
                	});
                	jQuery('.nmsTooltip').tooltipster({
                    	content: function() {
                    		return helpContentTemplate(helpContentService.nodeInfoDetails.nms);
                    	},
                    	position: "bottom",
                    	trigger: "click"
                    });
                	
            		var compiledTemplate = Handlebars.compile( [
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
					].join('') );

                	var getFirstInstanceOfServer = function( list ) {
                		var a = [];
                		var nestedA = [];

                		for( var p in list ) {
                			a.push( p );
                		}
                		for( var p2 in list[a.sort()[0]].instanceList) {
                			nestedA.push( p2 );
                		}
                		return nestedA.sort()[0];
                	};
                	var getFirstInstanceOfLink = function( list ) {
                		var a = [];
                		for( var p in list ) {
                			a.push( p );
                		}
                		return a.sort()[0];
                	}
                	
                	var showChart = function( histogram, timeSeriesHistogram ) {
                		if ( bInitialized ) {
                			scope.$broadcast('responseTimeChartDirective.updateData.forServerList', histogram);
                    		scope.$broadcast('loadChartDirective.updateData.forServerList', timeSeriesHistogram);
                		} else {
                			scope.$broadcast('responseTimeChartDirective.initAndRenderWithData.forServerList', histogram, '360px', '180px', false, true);
                    		scope.$broadcast('loadChartDirective.initAndRenderWithData.forServerList', timeSeriesHistogram, '360px', '200px', false, true);
                		}
                		
                	}
                	scope.showNMSList = false;
                	scope.showNodeServer = false;
                	scope.showLinkServer = false;
                	scope.selectServer = function( instanceName ) {
                		if ( bIsNode ) {
                    		showChart( scope.node.agentHistogram[instanceName], scope.node.agentTimeSeriesHistogram[instanceName] );                			
                		} else {
                    		showChart( scope.node.sourceHistogram[instanceName], scope.node.sourceTimeSeriesHistogram[instanceName] );
                		}
                	};
                	scope.invokeLinkAction = function( name, value ) {
                		if ( bAjaxLoading === true ) return;
                		bAjaxLoading = true;
                		if ( scope.showNMSList === true && $nms.attr("data-server") === value ) {
            				$nms.parent().scrollTop(0);
                			scope.showNMSList = false;
                			bAjaxLoading = false;
                		} else {
                			$nms.attr("data-server", value);
	                		ajaxService.getNMSData( value, function( result ) {
	                			$nms.empty();
	                			if ( angular.isDefined(result.errorCode) ) {
	                				$nms.html('<h4 style="text-align:center;padding-top:20%;text-decoration:red;text-decoration-color:orange">' + result.errorMessage + '</h4>');
	                			} else {
	                				$nms.html( compiledTemplate({ "datum": result }) );
	                			}
	                			scope.$apply(function() {
	                				scope.showNMSList = true;
	                			});
	                			bAjaxLoading = false;
	                		});
                		}
                	};
                	
                    scope.$on('serverListDirective.show', function ( event, bIsNodeServerList, node, oNavbarVoService ) {
                    	bIsNode = bIsNodeServerList;
                		scope.node = node;
                		scope.oNavbarVoService = oNavbarVoService;
                		var firstInstanceName = "";
                		var $radio = null;
                		showModal(); 
                    	if ( bIsNodeServerList ) {
                    		scope.serverList = node.serverList;
                    		scope.showNodeServer = true;
                    		scope.showLinkServer = false;
                    		                    		
                    		firstInstanceName = getFirstInstanceOfServer( scope.serverList );
                    		$radio = $element.find(".server-list input[type='radio']");
                    		showChart( scope.node.agentHistogram[firstInstanceName], scope.node.agentTimeSeriesHistogram[firstInstanceName] );
                    	} else {
                    		scope.linkList = scope.node.sourceHistogram;
                    		scope.showNodeServer = false;
                    		scope.showLinkServer = true;

                    		firstInstanceName = getFirstInstanceOfLink( scope.linkList );
                    		$radio = $element.find(".link-list input[type='radio']");
                    		showChart( scope.node.sourceHistogram[firstInstanceName], scope.node.sourceTimeSeriesHistogram[firstInstanceName] );
                    	}
                    	if ( $radio.length > 0 ) {
                   			$radio.filter(":first").prop("checked", true);
                			$element.find(".server-wrapper").scroll(0);
                		}
                		
                		bInitialized = true;
                    });

                    $element.find('.serverListTooltip').tooltipster({
                    	content: function() {
                    		return helpContentTemplate(helpContentService.nodeInfoDetails.nodeServers);
                    	},
                    	position: "bottom",
                    	trigger: "click"
                    });	
                }
            };
	    }
	]);
})();