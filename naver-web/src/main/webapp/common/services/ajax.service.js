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
	});
	
	pinpointApp.service('AjaxService', [ 'AjaxServiceConfig', function(cfg) {
	
	    var self = this;
	
	    /**
	     * get nms data
	     * @param query
	     * @param callback
	     */
	     this.getNMSData = function(url, cb) {
	        jQuery.ajax({
	            type: 'GET',
	            url: url,
	            cache: false,
	            dataType: 'json',
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