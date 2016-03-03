(function() {
	'use strict';
	/**
	 * (en)CommonAjaxService
	 * @ko CommonAjaxService
	 * @group Service
	 * @name CommonAjaxService
	 * @class
	 */
	pinpointApp.constant( "CommonAjaxServiceConfig", {
	});
	
	pinpointApp.service( "CommonAjaxService", [ "CommonAjaxServiceConfig", "$http", function( cfg, $http ) {
	
	    /**
	     * get nms data
	     * @param query
	     * @param callback
	     */
	     this.getNMSData = function(url, cb) {
			 $http.get( url).success( function( result ) {
				 cb(result);
			 }).error( function() {
				 cb(error);
			 });
	        //jQuery.ajax({
	        //    type: 'GET',
	        //    url: url,
	        //    cache: false,
	        //    dataType: 'json',
	        //    success: function (result) {
	        //        if (angular.isFunction(cb)) {
	        //            cb(result);
	        //        }
	        //    },
	        //    error: function (xhr, status, error) {
	        //        if (angular.isFunction(cb)) {
	        //            cb(error);
	        //        }
	        //    }
	        //});
	    };
		this.getSQLBind = function(url, data, cb) {
			$http.post( url, data).success( function( result ) {
				cb( result );
			}).error( function( error ) {
				cb( error );
			});
			//jQuery.ajax({
			//	type: 'POST',
			//	url: url,
			//	data: data,
			//	cache: false,
			//	dataType: 'json',
			//	success: function (result) {
			//		if (angular.isFunction(cb)) {
			//			cb(result);
			//		}
			//	},
			//	error: function (xhr, status, error) {
			//		if (angular.isFunction(cb)) {
			//			cb(error);
			//		}
			//	}
			//});
		};
		this.getServerTime = function( cb ) {
			$http.get( "/serverTime.pinpoint" ).success(function ( data ) {
				cb( data.currentServerTime );
			}).error( function () {
				cb( Date.now() );
			});
		};
		this.getApplicationList = function( cbSuccess, cbFail ) {
			$http.get(cfg.applicationUrl).success(function ( data ) {
				cbSuccess( data );
			}).error(function () {
				cbFail();
			});
		};
	}]);
})();