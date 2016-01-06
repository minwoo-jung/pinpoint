(function() {
	'use strict';
	/**
	 * (en)SQLAjaxService 
	 * @ko SQLAjaxService
	 * @group Service
	 * @name SQLAjaxService
	 * @class
	 */
	pinpointApp.constant('SQLAjaxServiceConfig', {
	});
	
	pinpointApp.service('SQLAjaxService', [ 'SQLAjaxServiceConfig', function(cfg) {
	
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
		this.getSQLBind = function(url, data, cb) {
			jQuery.ajax({
				type: 'POST',
				url: url,
				data: data,
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