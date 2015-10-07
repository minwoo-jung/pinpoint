(function() {
	'use strict';
	/**
	 * (en)AjaxService 
	 * @ko AjaxService
	 * @group Service
	 * @name AjaxService
	 * @class
	 */
	pinpointApp.constant('AjaxServiceConfig', {
	    nmsUrl: "nms.pinpoint"
	});
	
	pinpointApp.service('AjaxService', [ 'AjaxServiceConfig', function(cfg) {
	
	    var self = this;
	
	    /**
	     * get nms data
	     * @param query
	     * @param callback
	     */
	     this.getNMSData = function(query, cb) {
	        jQuery.ajax({
	            type: 'GET',
	            url: cfg.nmsUrl,
	            cache: false,
	            dataType: 'json',
	            data: {
		            "hostIp": query
		        },
	            success: function (result) {
	                if (angular.isFunction(cb)) {
	                    cb(result);
	                }
	            },
	            error: function (xhr, status, error) {
	                if (angular.isFunction(cb)) {
	                    cb(error);
	                }
	            }
	        });
	    };
	}]);
})();