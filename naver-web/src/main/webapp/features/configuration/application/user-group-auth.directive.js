(function($) {
	"use strict";
	pinpointApp.constant( "userGroupAuthDirectiveConfig", {
		"roles": [{
			"view": "Manager",
			"key": "manager"
		},{
			"view": "User",
			"key": "user"
		},{
			"view": "Guest",
			"key": "guest"
		}],
		"authorityList": [{
			"key": "serverMapData",
			"view": "ServerMap 데이터 비노출"
		},{
			"key": "apiMetaData",
			"view": "API 데이터 비노출"
		},{
			"key": "paramMetaData",
			"view": "Parameter 데이터 비노출"
		},{
			"key": "sqlMetaData",
			"view": "SQL 데이터 비노출"
		}],
		HAS_NOT_AUTHORITY: "권한이 없습니다."
	});


	pinpointApp.directive( "userGroupAuthDirective", [ "userGroupAuthDirectiveConfig", "$http", "AlarmUtilService",
		function ( $cfg, $http, AlarmUtilService ) {
			return {
				restrict: 'EA',
				replace: true,
				templateUrl: 'features/configuration/application/userGroupAuth.html?v=' + G_BUILD_TIME,
				scope: true,
				link: function (scope, element, attr) {
					var $element = $(element);
					var $elGuide = $element.find(".some-guide");
					var $elWrapper = $element.find(".wrapper");
					var $elLoading = $element.find(".some-loading");
					var aEditNodes = [$element.find("tr._edit1"), $element.find("tr._edit2")];
					var $elAlert = $element.find(".some-alert");
					var $elSubView = $element.find("._view");
					var $workingNode = null;

					var currentApplicationId = "";
					var bIsLoaded = false;
					var userGroupAuthList = [];
					var myRole = getGuest();
					scope.prefix = "userGroupAuth_";
					scope.userGroupList = [];
					scope.roleList = $cfg.roles;
					scope.authorityList = $cfg.authorityList;



					function cancelPreviousWork() {
						$elSubView.addClass( "hide-me" );
						AddAuth.cancelAction(aEditNodes, hideEditArea);
						RemoveAuth.cancelAction(AlarmUtilService, $workingNode, hideEditArea);
						UpdateAuth.cancelAction(AlarmUtilService, $workingNode, aEditNodes, hideEditArea);
					}
					function getManager() {
						return $cfg.roles[0].key;
					}
					function getGuest() {
						return $cfg.roles[$cfg.roles.length - 1].key
					}
					function isManager( auth ) {
						return getManager() === auth;
					}
					function isGuest( auth ) {
						return getGuest() === auth;
					}
					function hasManagerGroup() {
						for( var i = 0 ; i < scope.userGroupAuthList.length ; i++ ) {
							if ( isManager( scope.userGroupAuthList[i].role ) ) {
								return true;
							}
						}
						return false;
					}
					function hasAuthority() {
						if ( isManager( myRole ) ) {
							return true;
						}
						showAlert({
							errorMessage: $cfg.HAS_NOT_AUTHORITY
						});
						return false;
					}
					function hasAddAuthority() {
						if ( isManager( myRole ) ) {
							return true;
						}
						if ( isGuest( myRole ) && hasManagerGroup() === false ) {
							return true;
						}
						showAlert({
							errorMessage: $cfg.HAS_NOT_AUTHORITY
						});
						return false;
					}

					function isSameNode($current) {
						return AlarmUtilService.extractID($workingNode) === AlarmUtilService.extractID($current);
					}

					function showAlert(oServerError) {
						$elAlert.find(".message").html(oServerError.errorMessage);
						AlarmUtilService.hide($elLoading);
						AlarmUtilService.show($elAlert);
					}

					function loadData() {
						AlarmUtilService.show($elLoading);
						Remote.load( $http, {
							"applicationId": currentApplicationId.split("@")[0]
						}, function( oServerData ) {
							if ( oServerData.errorCode ) {
								showAlert( oServerData );
							} else {
								bIsLoaded = true;
								setMyRole( oServerData.myRole );
								userGroupAuthList = oServerData.userGroupAuthList;
								sortByRole( userGroupAuthList );
								scope.userGroupAuthList = userGroupAuthList;
								AlarmUtilService.hide($elLoading);
							}
						}, showAlert);
					}
					var oSortPoint = {
						"manager": 30,
						"user": 20,
						"guest": 10
					};
					function sortByRole( list ) {
						list.sort(function( a, b ) {
							return oSortPoint[b.role] - oSortPoint[a.role];
						});
						list.sort(function( a, b ) {
							if ( a.role === b.role ) {
								if ( a.userGroupId < b.userGroupId ) {
									return -1;
								} else if ( a.userGroupId > b.userGroupId ) {
									return 1;
								}
								return 0;
							}
							return 0;
						});
					}
					function setMyRole( newRole ) {
						myRole = newRole;
						scope.$emit( "changed.role", isManager( myRole ) );
					}
					function loadUserGroup() {
						AlarmUtilService.sendCRUD( "getUserGroupList", {}, function( aServerData ) {
							scope.userGroupList = aServerData;
						}, showAlert );
					}
					function showAddArea() {
						aEditNodes[0].find("select[name=userGroup]").prop( "disabled", "" ).show();
						aEditNodes[0].find("input").hide();
						if ( isManager( myRole ) ) {
							aEditNodes[0].find("select[name=role]").val( "" ).prop( "disabled", "" ).show();
						} else {
							aEditNodes[0].find("select[name=role]").val( getManager() ).prop( "disabled", "disabled" ).show();
						}
						aEditNodes[1].find("input[type=checkbox]").prop( "checked", false );
						$elWrapper.find("tbody").prepend( aEditNodes[1] ).prepend( aEditNodes[0] );
						AlarmUtilService.hide( aEditNodes[0].find( CONSTS.DIV_EDIT ) );
						AlarmUtilService.show( aEditNodes[0].find( CONSTS.DIV_ADD ) );
						$.each( aEditNodes, function( index, $el ) {
							AlarmUtilService.show( $el );
						});
					}
					function showEditArea( oAuth ) {
						AlarmUtilService.hide( aEditNodes[0].find( CONSTS.DIV_ADD ) );
						AlarmUtilService.show( aEditNodes[0].find( CONSTS.DIV_EDIT ) );
						if ( isGuest( oAuth.role ) ) {
							aEditNodes[0].find("select[name=userGroup]").hide();
							aEditNodes[0].find("select[name=role]").hide();
							aEditNodes[0].find("input").show();
						} else {
							aEditNodes[0].find("input").hide();
							aEditNodes[0].find("select[name=userGroup]").show().val( oAuth.userGroupId ).prop( "disabled", "disabled" );
							aEditNodes[0].find("select[name=role]").show().val( oAuth.role );
						}
						for( var p in oAuth.configuration ) {
							aEditNodes[1].find("input[name=" + p + "]").prop("checked", oAuth.configuration[p] );
						}
						$.each( aEditNodes, function( index, $el ) {
							AlarmUtilService.show( $el );
						});
					}
					function hideEditArea() {
						$.each( aEditNodes, function( index, $el ) {
							AlarmUtilService.hide( $el );
						});
						aEditNodes[0].find("select[name=userGroup]").val( "" );
						aEditNodes[0].find("select[name=role]").val( "" );
						aEditNodes[1].find("input[type=checkbox]").attr( "checked", false );
					}
					function getNewAuth( bIsUpdate ) {
						var userGroupId = aEditNodes[0].find("select[name=userGroup]").val();
						var roleId = aEditNodes[0].find("select[name=role]").val();

						return {
							"applicationId": currentApplicationId.split("@")[0],
							"userGroupId": userGroupId === "" && bIsUpdate === true ? getGuest() : userGroupId,
							"role": roleId === "" && bIsUpdate === true ? getGuest() : roleId,
							"configuration": (function( list ) {
								var o = {};
								$.each( list, function( index, value ) {
									o[value.key] = aEditNodes[1].find("input[name=" + value.key + "]").prop("checked") || false;
								});
								return o;
							})( scope.authorityList )
						};
					}
					function addOKRemove( $el, bView ) {
						$el.removeClass( "glyphicon-unchecked glyphicon-check" ).addClass( bView ? "glyphicon-check" : "glyphicon-unchecked" );
					}
					scope.isGuest = function( role ) {
						return isGuest( role );
					};
					scope.onViewAuth = function( $event, index ) {
						var $node = AlarmUtilService.getNode( $event, "tr" );
						if ( $workingNode !== null && isSameNode( $node ) === true && $elSubView.hasClass("hide-me") === false ) {
							console.log( "null or same", $workingNode );
							cancelPreviousWork();
							$workingNode = null;
							$elSubView.addClass( "hide-me" );
							return;
						}
						cancelPreviousWork();
						$workingNode = $node;
						$workingNode.after( $elSubView );
						for( var p in userGroupAuthList[index].configuration ) {
							addOKRemove( $elSubView.find( "span." + p ), userGroupAuthList[index].configuration[p] );
						}
						$elSubView.removeClass( "hide-me" );
					};
					scope.onAddAuth = function() {
						if ( currentApplicationId === "" ) {
							showAlert({
								errorMessage: "Application을 먼저 선택해 주세요"
							});
							return;
						}
						if ( hasAddAuthority() === false ) {
							return;
						}
						if ( AddAuth.isOn() ) {
							return;
						}
						cancelPreviousWork();
						AddAuth.onAction( function() {
							showAddArea();
						});
					};
					function onApplyAddAuth() {
						AddAuth.applyAction( AlarmUtilService, getNewAuth( false ), aEditNodes, $elLoading, function( applicationId, userGroupId ) {
							for( var i = 0 ; i < userGroupAuthList.length ; i++ ) {
								var oAuth = userGroupAuthList[i];
								if ( oAuth.applicationId === applicationId && oAuth.userGroupId === userGroupId ) {
									return true;
								}
							}
							return false;
						},function( oNewAuth, newMyRole ) {
							// AnalyticsService.sendMain( AnalyticsService.CONST.CLK_ALARM_CREATE_RULE );
							setMyRole( newMyRole );
							userGroupAuthList.push( oNewAuth );
							sortByRole( userGroupAuthList );
							scope.userGroupAuthList = userGroupAuthList;
							hideEditArea();
						}, showAlert, $http );
					}
					function onCancelAddAuth() {
						AddAuth.cancelAction( aEditNodes, hideEditArea );
					}
					function onRemoveAuth( $target ) {
						if ( hasAuthority() === false ) {
							return;
						}
						cancelPreviousWork();
						var $node = $target.parents("tr");
						$workingNode = $node;
						RemoveAuth.onAction( AlarmUtilService, $workingNode );
					}
					function onCancelRemoveAuth() {
						RemoveAuth.cancelAction( AlarmUtilService, $workingNode );
					}
					function onApplyRemoveAuth( index ) {
						var oAuth = userGroupAuthList[index];
						RemoveAuth.applyAction( AlarmUtilService, $workingNode, $elLoading, oAuth, function( applicationId, userGroupId, newMyRole ) {
							for( var i = 0 ; i < userGroupAuthList.length ; i++ ) {
								var oAuth = userGroupAuthList[i];
								if ( oAuth.applicationId == applicationId && oAuth.userGroupId == userGroupId ) {
									userGroupAuthList.splice(i, 1);
									break;
								}
							}
							setMyRole( newMyRole );
							scope.$apply(function() {
								scope.userGroupAuthList = userGroupAuthList;
							});
						}, showAlert );
					}
					function onUpdateAuth( $target, index ) {
						if ( hasAuthority() === false ) {
							return;
						}
						cancelPreviousWork();
						$workingNode = $target.parents("tr");
						UpdateAuth.onAction( AlarmUtilService, $workingNode, function() {
							$workingNode.after( aEditNodes[1] ).after( aEditNodes[0] );
							showEditArea( userGroupAuthList[index] );
						});
					}
					function onCancelUpdateAuth() {
						UpdateAuth.cancelAction( AlarmUtilService, $workingNode, aEditNodes, hideEditArea );
					}
					function onApplyUpdateAuth() {
						UpdateAuth.applyAction( AlarmUtilService, getNewAuth( true ), aEditNodes, $workingNode, $elLoading, function( applicationId, userGroupId ) {
							for( var i = 0 ; i < userGroupAuthList.length ; i++ ) {
								var oAuth = userGroupAuthList[i];
								if ( oAuth.applicationId === applicationId && oAuth.userGroupId === userGroupId ) {
									return true;
								}
							}
							return false;
						},function( oUpdateAuth, newMyRole ) {
							hideEditArea();
							for( var i = 0 ; i < userGroupAuthList.length ; i++ ) {
								if ( userGroupAuthList[i].userGroupId == oUpdateAuth.userGroupId ) {
									userGroupAuthList[i].role = oUpdateAuth.role;
									for( var p in oUpdateAuth.configuration ) {
										userGroupAuthList[i].configuration[p] = oUpdateAuth.configuration[p];
									}
								}
							}
							setMyRole( newMyRole );
							scope.userGroupAuthList = userGroupAuthList;
						}, showAlert, $http );
					}
					scope.$on("applicationGroup.sub.load", function( event, appId, invokeCount ) {
						currentApplicationId = appId;
						cancelPreviousWork();
						AlarmUtilService.hide( $elGuide );
						loadData();
						if ( invokeCount === 0 ) {
							loadUserGroup();
						}
					});
					scope.onCloseAlert = function() {
						AlarmUtilService.hide( $elAlert );
					};
					scope.grabAction = function( $event ) {
						if ( $event.target.tagName.toLowerCase() === "span" ) {
							var $target = $( $event.target );
							switch( $target.parent().attr("class") ) {
								case "_normal":
									if ( $target.hasClass( "edit" ) ) {
										onUpdateAuth( $target, $target.parent().parent().attr("data-index") );
									} else if ( $target.hasClass( "remove" ) ) {
										onRemoveAuth( $target );
									}
									break;
								case "_remove":
									if ( $target.hasClass( "remove-cancel" ) ) {
										onCancelRemoveAuth();
									} else if ( $target.hasClass( "remove-confirm" ) ) {
										onApplyRemoveAuth( $target.parent().parent().attr("data-index") );
									}
									break;
								case "_edit":
									if ( $target.hasClass( "edit-confirm" ) ) {
										onApplyUpdateAuth();
									} else if ( $target.hasClass( "edit-cancel" ) ) {
										onCancelUpdateAuth();
									}
									break;
								case "_add":
									if ( $target.hasClass( "add-confirm" ) ) {
										onApplyAddAuth();
									} else if ( $target.hasClass( "add-cancel" ) ) {
										onCancelAddAuth();
									}
									break;
							}
						}
					}
				}
			};
		}
	]);
	var CONSTS = {
		SELECT_USER_GROUP_AND_AUTHORITY: "Select user group and role.",
		EXIST_A_SAME: "Exist a same user group in the list",
		DIV_NORMAL: "div._normal",
		DIV_REMOVE: "div._remove",
		DIV_ADD: "div._add",
		DIV_EDIT: "div._edit"
	};

	var AddAuth = {
		_bIng: false,
		isOn: function() {
			return this._bIng;
		},
		onAction: function( cb ) {
			this._bIng = true;
			cb();
		},
		cancelAction: function( aEditNodes, cbCancel ) {
			if ( this._bIng === true ) {
				// $.each( aEditNodes, function( index, $el ) {
				// 	$el.removeClass("blink-blink");
				// });
				cbCancel();
				this._bIng = false;
			}
		},
		applyAction: function( AlarmUtilService, oNewAuth, aEditNodes, $elLoading, cbHasSameAuth, cbSuccess, cbFail, $http ) {
			var self = this;
			AlarmUtilService.show( $elLoading );
			if ( oNewAuth.userGroupId === "" || oNewAuth.role === "" ) {
				// $.each( aEditNodes, function( index, $el ) {
				// 	$el.addClass("blink-blink");
				// });
				cbFail({ errorMessage: CONSTS.SELECT_USER_GROUP_AND_AUTHORITY });
				return;
			}
			if ( cbHasSameAuth( oNewAuth.applicationId, oNewAuth.userGroupId ) ) {
				// $.each( aEditNodes, function( index, $el ) {
				// 	$el.addClass("blink-blink");
				// });
				cbFail({ errorMessage: CONSTS.EXIST_A_SAME });
				return;
			}
			Remote.add( $http, oNewAuth, function( oServerData ) {
				if ( oServerData.errorCode ) {
					cbFail( oServerData );
				} else {
					oNewAuth.ruleId = oServerData.ruleId;
					cbSuccess( oNewAuth, oServerData.myRole );
					self.cancelAction(aEditNodes, function () {});
					AlarmUtilService.hide($elLoading);
				}
			});
		}
	};
	var RemoveAuth = {
		_bIng: false,
		onAction: function( AlarmUtilService, $node ) {
			this._bIng = true;
			$node.addClass("remove");
			AlarmUtilService.hide( $node.find( CONSTS.DIV_NORMAL ) );
			AlarmUtilService.show( $node.find( CONSTS.DIV_REMOVE ) );
		},
		cancelAction: function( AlarmUtilService, $node ) {
			if ( this._bIng === true ) {
				$node.removeClass("remove");
				AlarmUtilService.hide($node.find( CONSTS.DIV_REMOVE ));
				AlarmUtilService.show($node.find( CONSTS.DIV_NORMAL ));
				this._bIng = false;
			}
		},
		applyAction: function( AlarmUtilService, $node, $elLoading, oAuth, cbSuccess, cbFail ) {
			var self = this;
			AlarmUtilService.show( $elLoading );
			Remote.remove( {
				"applicationId": oAuth.applicationId,
				"userGroupId": oAuth.userGroupId
			}, function( oServerData ) {
				if (oServerData.errorCode) {
					cbFail(oServerData);
				} else {
					self.cancelAction(AlarmUtilService, $node);
					cbSuccess( oAuth.applicationId, oAuth.userGroupId, oServerData.myRole );
					AlarmUtilService.hide($elLoading);
				}
			});
		}
	};
	var UpdateAuth = {
		_bIng: false,
		onAction: function( AlarmUtilService, $node, cb ) {
			this._bIng = true;
			AlarmUtilService.hide( $node );
			cb();
		},
		cancelAction: function( AlarmUtilService, $node, aEditNodes, cbCancel ) {
			if ( this._bIng === true ) {
				cbCancel();
				$.each( aEditNodes, function( index, $el ) {
					$el.removeClass("blink-blink");
				});
				AlarmUtilService.show( $node );
				this._bIng = false;
			}
		},
		applyAction: function( AlarmUtilService, oUpdateAuth, aEditNodes, $node, $elLoading, cbHasSameAuth, cbSuccess, cbFail, $http ) {
			var self = this;
			AlarmUtilService.show($elLoading);
			// if ( cbHasSameAuth( oUpdateAuth.applicationId, oUpdateAuth.userGroupId ) ) {
			// 	// $.each( aEditNodes, function( index, $el ) {
			// 	// 	$el.addClass("blink-blink");
			// 	// });
			// 	cbFail({errorMessage: CONSTS.EXIST_A_SAME});
			// 	return;
			// }
			Remote.update( $http, oUpdateAuth, function( oServerData ) {
				if (oServerData.errorCode) {
					cbFail(oServerData);
				} else {
					self.cancelAction(AlarmUtilService, $node, aEditNodes, function () {});
					cbSuccess( oUpdateAuth, oServerData.myRole );
					AlarmUtilService.hide($elLoading);
				}
			});
		}
	};
	var Remote = {
		"URL": "application/userGroupAuth.pinpoint",
		"add": function( $http, data, callback ) {
			$http.post( Remote.URL, data )
				.then(function(result) {
					callback(result.data);
				}, function(error) {
					callback(error);
				});
		},
		"update": function( $http, data, callback ) {
			$http.put( Remote.URL, data )
				.then(function(result) {
					callback(result.data);
				}, function(error) {
					callback(error);
				});
		},
		"remove": function( data, callback) {
			$.ajax( Remote.URL, {
				type: "DELETE",
				data: JSON.stringify(data),
				contentType: "application/json"
			}).done(function(result) {
				callback(result);
			}).fail(function(error) {
				callback(error);
			});
		},
		"load": function( $http, data, callback ) {
			$http.get( Remote.URL + "?" + $.param( data ) ).then( function( result ) {
				callback( result.data );
			}, function(error) {
				callback( error );
			});
		}
	};
})(jQuery);