(function() {
	'use strict';
	var oHelp = {
		configuration: {
			general: {
				warning: "(User configuration is stored in browser cache. Server-side storage will be supported in a future release.)",
				empty: "Favorite list empty"
			},
			alarmRules: {
				mainStyle: "",
				title: "Alarm Rule Type",
				desc: "The following types of alarm rules are supported by Pinpoint.",
				category: [{
					title: "[Type]",
					items: [{
						name: "SLOW COUNT",
						desc: "Sends an alarm when the number of slow requests sent by the application exceeds the configured threshold."
					},{
						name: "SLOW RATE",
						desc: "Sends an alarm when the percentage(%) of slow requests sent by the application exceeds the configured threshold."
					},{
						name: "ERROR COUNT",
						desc: "Sends an alarm when the number of failed requests sent by the application exceeds the configured threshold."
					},{
						name: "ERROR RATE",
						desc: "Sends an alarm when the percentage(%) of failed requests sent by the application exceeds the configured threshold."
					},{
						name: "TOTAL COUNT",
						desc: "Sends an alarm when the number of all requests sent by the application exceeds the configured threshold."
					},{
						name: "SLOW COUNT TO CALLEE",
						desc: "Sends an alarm when the number of slow responses returned by the application exceeds the configured threshold."
					},{
						name: "SLOW RATE TO CALLEE",
						desc: "Sends an alarm when the percentage(%) of slow responses returned by the application exceeds the configured threshold."
					},{
						name: "ERROR COUNT TO CALLEE",
						desc: "Sends an alarm when the number of failed responses returned by the application exceeds the configured threshold."
					},{
						name: "ERROR RATE TO CALLEE",
						desc: "Sends an alarm when the percentage(%) of failed responses returned by the application exceeds the configured threshold."
					},{
						name: "TOTAL COUNT TO CALLEE",
						desc: "Sends an alarm when the number of all remote calls sent to the application exceeds the configured threshold."
					},{
						name: "HEAP USAGE RATE",
						desc: "Sends an alarm when the application's heap usage(%) exceeds the configured threshold."
					},{
						name: "JVM CPU USAGE RATE",
						desc: "Sends an alarm when the application's CPU usage(%) exceeds the configured threshold."
					},{
						name: "DATASOURCE CONNECTION USAGE RATE",
						desc: "Sends an alarm when the application's DataSource connection usage(%) exceeds the configured threshold."
					}, {
						name: "DEADLOCK OCCURRENCE",
						desc: "Sends an alarm when deadlock condition is detected in application."
					}]
				}]
			},
			installation: {
				desc: "* You can check whether the Application Name and Agent Id are duplicated.",
				lengthGuide: "You can enter up to {{MAX_CHAR}} characters."
			}
		},	
		navbar : {
			searchPeriod : {
				guideDateMax: "Search duration may not be greater than {{day}} days.",
				guideDateOrder: "Date or time set incorrectly"
			},
			applicationSelector: {
				mainStyle: "",
				title: "Application List",
				desc: "Shows the list of applications with Pinpoint installed.",
				category : [{
					title: "[Legend]",
					items: [{
						name: "Icon",
						desc: "Application Type"
					}, {
						name: "Text",
						desc: "Application Name. The value set using <code>-Dpinpoint.applicationName</code> when launching Pinpoint agent."
					}]
				}]
			},
			depth : {
				mainStyle: "",
				title: '<img src="images/inbound.png" width="22px" height="22px" style="margin-top:-4px;"> Inbound and <img src="images/outbound.png" width="22px" height="22px" style="margin-top:-4px"> Outbound',
				desc: "Search-depth of server map.",
				category : [{
					title: "[Legend]",
					items: [{
						name: "Inbound",
						desc: "Number of depth to render for requests coming in to the selected node."
					}, {
						name: "Outbound",
						desc: "Number of depth to render for requests going out from the selected node"
					}]
				}]
			},
			bidirectional : {
				mainStyle: "",
				title: '<img src="images/bidirect_on.png" width="22px" height="22px" style="margin-top:-4px;"> Bidirectional Search',
				desc: "Search-method of server map.",
				category : [{
					title: "[Legend]",
					items: [{
						name: "Bidirectional",
						desc: "Renders inbound/outbound nodes for each and every node (within limit) even if they are not directly related to the selected node.<br>Note that checking this option may lead to overly complex server maps."
					}]
				}]
			},
			periodSelector: {
				mainStyle: "",
				title: "Period Selector",
				desc: "Selects the time period for querying data.",
				category: [{
					title: "[Usage]",
					items: [{
						name: "<button type='button' class='btn btn-success btn-xs'><span class='glyphicon glyphicon-th-list'></span></button>",
						desc: "Query for data traced during the most recent selected time-period.<br/>Auto-refresh is supported for 5m, 10m, 3h time-period."
					},{
						name: "<button type='button' class='btn btn-success btn-xs'><span class='glyphicon glyphicon-calendar'></span></button>",
						desc: "Query for data traced between the two selected times for a maximum of 48 hours."
					}]
				}]
			}
		},
		servermap : {
			"default": {
				mainStyle: "width:560px;",
				title: "Server Map",
				desc: "Displays a topological view of the distributed server map.",
				category: [{
					title: "[Node]",
					list: [
				       "Each node is a logical unit of application.",
				       "The value on the top-right corner represents the number of server instances assigned to that application. (Not shown when there is only one such instance)",
				       "An alarm icon is displayed on the top-left corner if an error/exception is detected in one of the server instances.",
				       "Clicking a node shows information on all incoming transactions on the right-hand side of the screen."
					]
				},{
					title: "[Arrow]",
					list: [
						"Each arrow represents a transaction flow.",
						"The number shows the transaction count and is displayed in red for transactions with error.",
						"<span class='glyphicon glyphicon-filter' style='color:green;'></span> is shown when a filter is applied.",
						"Clicking an arrow shows information on all transactions passing through the selected section on the right-hand side of the screen."
				    ]
				},{
					title: "[Function of the box]",
					list: [
						"When the box is selected, the transaction information flowing into the application is displayed on the right side of the screen."
					]
				},{
					title: "[Function of the arrow]",
					list: [
						"Select the arrow to show the transaction information that passes through the selected section on the right side of the screen.",
						"The Filter in the Context menu shows only the transactions that pass through the selected section.",
						"Filter wizard allows you to configure more detailed filters.",
						"When the filter is applied, <span class = 'glyphicon glyphicon-filter' style = 'color: green;'> </span> icon will be displayed on the arrow."
					]
				},{
					title: "[Applying Filter]",
					list: [
				        "Right-clicking on an arrow displays a filter menu.",
				        "'Filter' filters the server map to only show transactions that has passed through the selected section.",
				        "'Filter Wizard' allows additional filter configurations."
					]
				},{
					title: "[Chart Configuration]",
					list: [
				        "Right-clicking on an empty area displays a chart configuration menu.",
				        "Node Setting / Merge Unknown : Groups all agent-less applications into a single node.",
				        "Double-clicking on an empty resets the zoom level of the server map."
					]
				}]
			} 
		},
		scatter : {
			"default": {
				mainStyle: "",
				title: "Response Time Scatter Chart",
				desc: "",
				category: [{
					title: "[Legend]",
					items: [{
						name: "<span class='glyphicon glyphicon-stop' style='color:#2ca02c'></span>",
						desc: "Successful Transaction"
					},{
						name: "<span class='glyphicon glyphicon-stop' style='color:#f53034'></span>",
						desc: "Failed Transaction"
					},{
						name: "X-axis",
						desc: "Transaction Timestamp (hh:mm)"
					},{
						name: "Y-axis",
						desc: "Response Time (ms)"
					}]
				},{
					title: "[Usage]",
					image: "<img src='images/help/scatter_01.png' width='200px' height='125px'>",
					items: [{
						name: "<span class='glyphicon glyphicon-plus'></span>",
						desc: "Drag on the scatter chart to show detailed information on selected transactions."
					},{
						name: "<span class='glyphicon glyphicon-cog'></span>",
						desc: "Set the min/max value of the Y-axis (Response Time)."
					},{
						name: "<span class='glyphicon glyphicon-download-alt'></span>",
						desc: "Download the chart as an image file."
					},{
						name: "<span class='glyphicon glyphicon-fullscreen'></span>",
						desc: "Open the chart in a new window."
					}]
				}]
			}
		},
		realtime: {
			"default": {
				mainStyle: "",
				title: "Realtime Active Thread Chart",
				desc: "Shows the Active Thread count of each agent in realtime.",
				category: [{
					title: "[Error Messages]",
					items: [{
						name: "UNSUPPORTED VERSION",
						desc: "Agent version too old. (Please upgrade the agent to 1.5.0+)",
						nameStyle: "width:120px;border-bottom:1px solid gray",
						descStyle: "border-bottom:1px solid gray"
					},{
						name: "CLUSTER OPTION NOTSET",
						desc: "Option disabled by agent. (Please set profiler.pinpoint.activethread to true in profiler.config)",
						nameStyle: "width:120px;border-bottom:1px solid gray",
						descStyle: "border-bottom:1px solid gray"
					},{
						name: "TIMEOUT",
						desc: "Agent connection timed out receiving active thread count. Please contact the administrator if problem persists.",
						nameStyle: "width:120px;border-bottom:1px solid gray",
						descStyle: "border-bottom:1px solid gray"
					},{
						name: "NOT FOUND",
						desc: "Agent not found. (If you get this message while the agent is running, please set profiler.tcpdatasender.command.accept.enable to true in profiler.config)",
						nameStyle: "width:120px;border-bottom:1px solid gray",
						descStyle: "border-bottom:1px solid gray"
					},{
						name: "CLUSTER CHANNEL CLOSED",
						desc: "Agent session expired.",
						nameStyle: "width:120px;border-bottom:1px solid gray",
						descStyle: "border-bottom:1px solid gray"
					},{
						name: "PINPOINT INTERNAL ERROR",
						desc: "Pinpoint internal error. Please contact the administrator.",
						nameStyle: "width:120px;border-bottom:1px solid gray",
						descStyle: "border-bottom:1px solid gray"
					},{
						name: "No Active Thread",
						desc: "The agent has no threads that are currently active.",
						nameStyle: "width:120px;border-bottom:1px solid gray",
						descStyle: "border-bottom:1px solid gray"
					},{
						name: "No Response",
						desc: "No response from Pinpoint Web. Please contact the administrator.",
						nameStyle: "width:120px;border-bottom:1px solid gray",
						descStyle: "border-bottom:1px solid gray"
					}]
				}]
			}
		},
		nodeInfoDetails: {
			responseSummary: {
				mainStyle: "",
				title: "Response Summary Chart",
				desc: "",
				category: [{
					title: "[Legend]",
					items: [{
						name: "X-Axis",
						desc: "Response Time"
					},{
						name: "Y-Axis",
						desc: "Transaction Count"
					},{
						name: "<spanstyle='color:#2ca02c'>1s</span>",
						desc: "No. of Successful transactions (less than 1 second)"
					},{
						name: "<span style='color:#3c81fa'>3s</span>",
						desc: "No. of Successful transactions (1 ~ 3 seconds)"
					},{
						name: "<span style='color:#f8c731'>5s</span>",
						desc: "No. of Successful transactions (3 ~ 5 seconds)"
					},{
						name: "<span style='color:#f69124'>Slow</span>",
						desc: "No. of Successful transactions (greater than 5 seconds)"
					},{
						name: "<span style='color:#f53034'>Error</span>",
						desc: "No. of Failed transactions regardless of response time"
					}]
				}]
			},
			load: {
				mainStyle: "",
				title: "Load Chart",
				desc: "",
				category: [{
					title: "[Legend]",
					items: [{
						name: "X-Axis",
						desc: "Transaction Timestamp (in minutes)"
					},{
						name: "Y-Axis",
						desc: "Transaction Count"
					},{
						name: "<spanstyle='color:#2ca02c'>1s</span>",
						desc: "No. of Successful transactions (less than 1 second)"
					},{
						name: "<span style='color:#3c81fa'>3s</span>",
						desc: "No. of Successful transactions (1 ~ 3 seconds)"
					},{
						name: "<span style='color:#f8c731'>5s</span>",
						desc: "No. of Successful transactions (3 ~ 5 seconds)"
					},{
						name: "<span style='color:#f69124'>Slow</span>",
						desc: "No. of Successful transactions (greater than 5 seconds)"
					},{
						name: "<span style='color:#f53034'>Error</span>",
						desc: "No. of Failed transactions regardless of response time"
					}]
				},{
					title: "[Usage]",
					list: [
				       "Clicking on a legend item shows/hides all transactions within the selected group.",
				       "Dragging on the chart zooms in to the dragged area."
					]
				}]
			},
			nodeServers: {
				mainStyle: "width:400px;",
				title: "Server Information",
				desc: "List of physical servers and their server instances.",
				category: [{
					title: "[Legend]",
					items: [{
						name: "<span class='glyphicon glyphicon-home'></span>",
						desc: "Hostname of the physical server"
					},{
						name: "<span class='glyphicon glyphicon-hdd'></span>",
						desc: "AgentId of the Pinpoint agent installed on the server instance running on the physical server"
					}]
				},{
					title: "[Usage]",
					items: [{
						name: "<button type='button' class='btn btn-default btn-xs'>Inspector</button>",
						desc: "Open a new window with detailed information on the WAS with Pinpoint installed."
					},{
						name: "<span class='glyphicon glyphicon-record' style='color:#3B99FC'></span>",
						desc: "Display statistics on transactions carried out by the server instance."
					},{
						name: "<span class='glyphicon glyphicon-hdd' style='color:red'></span>",
						desc: "Display statistics on transactions (with error) carried out by the server instance."
					}]
				}]
			},
			unknownList: {
				mainStyle: "",
				title: "UnknownList",
				desc: "From the chart's top-right icon",
				category: [{
					title: "[Usage]",
					items: [{
						name: "1st",
						desc: "Toggle between Response Summary Chart / Load Chart"
					},{
						name: "2nd",
						desc: "Show Node Details"
					}]
				}]
			},
			searchAndOrder: {
				mainStyle: "",
				title: "Search and Filter",
				desc: "You can search with server name and Counts.",
				category: [{
					title: "[Usage]",
					items: [{
						name: "Name",
						desc: "Sort names in ascending / descending order."
					},{
						name: "Count",
						desc: "Sort counts in ascending / descending order."
					}]
				}]
			},
			nms: {
				mainStyle: "",
				title: "NMS",
				desc: "Network Management System",
				category: [{
					title: "[word]",
					items: [{
						name: "Connection Per Seconds",
						desc: "Connection Per Seconds"
					},{
						name: "Concurrent Connection",
						desc: "Concurrent Connection"
					}]
				}]
			}
		},
		linkInfoDetails: {
			responseSummary: {
				mainStyle: "",
				title: "Response Summary Chart",
				desc: "",
				category: [{
					title: "[Legend]",
					items: [{
						name: "X-Axis",
						desc: "Response Time"
					},{
						name: "Y-Axis",
						desc: "Transaction Count"
					},{
						name: "<spanstyle='color:#2ca02c'>1s</span>",
						desc: "No. of Successful transactions (less than 1 second)"
					},{
						name: "<span style='color:#3c81fa'>3s</span>",
						desc: "No. of Successful transactions (1 ~ 3 seconds)"
					},{
						name: "<span style='color:#f8c731'>5s</span>",
						desc: "No. of Successful transactions (3 ~ 5 seconds)"
					},{
						name: "<span style='color:#f69124'>Slow</span>",
						desc: "No. of Successful transactions (greater than 5 seconds)"
					},{
						name: "<span style='color:#f53034'>Error</span>",
						desc: "No. of Failed transactions regardless of response time"
					}]
				},{
					title: "[Usage]",
					list: ["Click on the bar to query for transactions within the selected response time."]
				}]
			},
			load: {
				mainStyle: "",
				title: "Load Chart",
				desc: "",
				category: [{
					title: "[Legend]",
					items: [{
						name: "X-Axis",
						desc: "Transaction Timestamp (in minutes)"
					},{
						name: "Y-Axis",
						desc: "Transaction Count"
					},{
						name: "<spanstyle='color:#2ca02c'>1s</span>",
						desc: "No. of Successful transactions (less than 1 second)"
					},{
						name: "<span style='color:#3c81fa'>3s</span>",
						desc: "No. of Successful transactions (1 ~ 3 seconds)"
					},{
						name: "<span style='color:#f8c731'>5s</span>",
						desc: "No. of Successful transactions (3 ~ 5 seconds)"
					},{
						name: "<span style='color:#f69124'>Slow</span>",
						desc: "No. of Successful transactions (greater than 5 seconds)"
					},{
						name: "<span style='color:#f53034'>Error</span>",
						desc: "No. of Failed transactions regardless of response time"
					}]
				},{
					title: "[Usage]",
					list: [
				       "Clicking on a legend item shows/hides all transactions within the selected group.",
				       "Dragging on the chart zooms in to the dragged area."
					]
				}]
			},
			linkServers: {
				mainStyle: "width:350px;",
				title: "Server Information",
				desc: "List of physical servers and their server instances.",
				category: [{
					title: "[Legend]",
					items: [{
						name: "<span class='glyphicon glyphicon-home'></span>",
						desc: "Hostname of the physical server"
					},{
						name: "<span class='glyphicon glyphicon-hdd'></span>",
						desc: "AgentId of the Pinpoint agent installed on the server instance running on the physical server"
					}]
				},{
					title: "[Usage]",
					items: [{
						name: "<button type='button' class='btn btn-default btn-xs'>Inspector</button>",
						desc: "Open a new window with detailed information on the WAS with Pinpoint installed."
					},{
						name: "<button type='button' class='btn btn-default btn-xs'><span class='glyphicon glyphicon-plus'></span></button>",
						desc: "Display statistics on transactions carried out by the server instance."
					},{
						name: "<button type='button' class='btn btn-danger btn-xs'><span class='glyphicon glyphicon-plus'></span></button>",
						desc: "Display statistics on transactions (with error) carried out by the server instance."
					}]
				}]
			},
			unknownList: {
				mainStyle: "",
				title: "UnknownList",
				desc: "From the chart's top-right icon,",
				category: [{
					title: "[Usage]",
					items: [{
						name: "1st",
						desc: "Toggle between Response Summary Chart"
					},{
						name: "2dn",
						desc: "Show Node Details"
					}]
				}]
			},
			searchAndOrder: {
				mainStyle: "",
				title: "Search and Filter",
				desc: "You can search with server name and Counts.",
				category: [{
					title: "[Usage]",
					items: [{
						name: "Name",
						desc: "Sort names in ascending / descending order."
					},{
						name: "Count",
						desc: "Sort counts in ascending / descending order."
					}]
				}]
			},
			nms: {
				mainStyle: "",
				title: "NMS",
				desc: "Network Management System",
				category: [{
					title: "[word]",
					items: [{
						name: "Connection Per Seconds",
						desc: "Connection Per Seconds"
					},{
						name: "Concurrent Connection",
						desc: "Concurrent Connection"
					}]
				}]
			}
		},
		inspector: {
			noDataCollected: "No data collected",
			list: {
				mainStyle: "",
				title: "Agent list",
				desc: "List of agents registered under the current Application Name",
				category: [{
					title: "[Legend]",
					items: [{
						name: "<span class='glyphicon glyphicon-home'></span>",
						desc: "Hostname of the agent's machine"
					},{
						name: "<span class='glyphicon glyphicon-hdd'></span>",
						desc: "Agent-id of the installed agent"
					},{
						name: "<span class='glyphicon glyphicon-ok-sign' style='color:#40E340'></span>",
						desc: "Agent was running at the time of query"
					},{
						name: "<span class='glyphicon glyphicon-minus-sign' style='color:#F00'></span>",
						desc: "Agent was shutdown at the time of query"
					},{
						name: "<span class='glyphicon glyphicon-remove-sign' style='color:#AAA'></span>",
						desc: "Agent was disconnected at the time of query"
					},{
						name: "<span class='glyphicon glyphicon-question-sign' style='color:#AAA'></span>",
						desc: "Agent status was unknown at the time of query"
					}]
				}]
			},
			heap: {
				mainStyle: "",
				title: "Heap",
				desc: "JVM's heap information and major garbage collection times(if any)",
				category: [{
					title: "[Legend]",
					items: [{
						name: "Max",
						desc: "Maximum heap size"
					},{
						name: "Used",
						desc: "Heap currently in use"
					},{
						name: "Major GC",
						desc: "Major garbage collection duration (number of Major GCs in parenthesis if it occurred more than once)"
					}]
				}]
			},
			permGen: {
				mainStyle: "",
				title: "Non-Heap",
				desc: "JVM's non-heap information and major garbage collection times(if any)",
				category: [{
					title: "[Legend]",
					items: [{
						name: "Max",
						desc: "Maximum non-heap size"
					},{
						name: "Used",
						desc: "Non-heap currently in use"
					},{
						name: "Major GC",
						desc: "Major garbage collection duration (number of Major GCs in parenthesis if it occurred more than once)"
					}]
				}]
			},
			cpuUsage: {
				mainStyle: "",
				title: "Cpu Usage",
				desc: "JVM/System's CPU Usage - For multi-core CPUs, displays the average CPU usage of all the cores",
				category: [{
					title: "[Legend]",
					items: [{
						name: "Java 1.6",
						desc: "Only the JVM's CPU usage is collected"
					},{
						name: "Java 1.7+",
						desc: "Both the JVM's and the system's CPU usage are collected"
					}]
				}]
			},
            tps: {
                mainStyle: "",
                title: "TPS",
                desc: "Transactions per second received by the server",
                category: [{
                    title: "[Legend]",
                    items: [{
                        name: "Sampled New (S.N)",
                        desc: "Profiled transactions that started from the current agent"
                    },{
                        name: "Sampled Continuation (S.C)",
                        desc: "Profiled transactions that started from another agent"
                    },{
                        name: "Unsampled New (U.N)",
                        desc: "Unprofiled transactions that started from the current agent"
                    },{
                        name: "Unsampled Continuation (U.C)",
                        desc: "Unprofiled transactions that started from another agent"
                    },{
                        name: "Skipped New (S.S.N)",
                        desc: "Unprofiled transactions that started from the selected agent because of processing limit"
                    },{
                        name: "Skipped Continuation (S.S.C)",
                        desc: "Unprofiled transactions that started from another agent because of processing limit"
                    },{
                        name: "Total",
                        desc: "All transactions"
                    }]
                }]
            },
			activeThread: {
				mainStyle: "",
				title: "Active Thread",
				desc: "Snapshots of the agent's active thread count, categorized by how long they have active for serving a request.",
				category: [{
					title: "[Legend]",
					items: [{
						name: "Fast (1s)",
						desc: "Number of threads that have been active for less than or equal to 1s"
					},{
						name: "Normal (3s)",
						desc: "Number of threads that have been active for less than or equal to 3s but longer than 1s"
					},{
						name: "Slow (5s)",
						desc: "Number of threads that have been active for less than or equal to 5s but longer than 3s"
					},{
						name: "Very Slow (slow)",
						desc: "Number of threads that have been active for longer than 5s"
					}]
				}]
			},
			dataSource: {
				mainStyle: "",
				title: "Data Source",
				desc: "Show the status of agent's data source.",
				category: [{
					title: "[Legend]",
					items: [{
						name: "Active Avg",
						desc: "Average number of active connections"
					},{
						name: "Active Max",
						desc: "Maximum number of active connections"
					},{
						name: "Total Max",
						desc: "The maximum number of active connections that can be allocated at the same time"
					},{
						name: "Type",
						desc: "DB Connection Pool Type"
					}]
				}]
			},
			responseTime: {
				mainStyle: "",
				title: "Response time",
				desc: "Shows the status of agent's response time.",
				category: [{
					title: "[Legend]",
					items: [{
						name: "Avg",
						desc: "Average Response Time (unit : milliseconds)"
					}]
				}]
			},
			openFileDescriptor: {
				mainStyle: "",
				title: "File Descriptor",
				desc: "Shows the status of agent's file descriptors.",
				category: [{
					title: "[Legend]",
					items: [{
						name: "Open File Descriptor",
						desc: "Number of open file descriptor currently used"
					}]
				}]
			},
			directBufferCount: {
				mainStyle: "",
				title: "Direct Buffer",
				desc: "Shows the status of agent's direct buffer",
				category: [{
					title: "[Legend]",
					items: [{
						name: "Direct Buffer Count",
						desc: "Number of direct buffer"
					}]
				}]
			},
			directBufferMemory: {
				mainStyle: "",
				title: "Direct Buffer Memory",
				desc: "Shows the status of agent's used direct buffer memory",
				category: [{
					title: "[Legend]",
					items: [{
						name: "Direct Buffer Memory Used",
						desc: "Currently used direct buffer memory"
					}]
				}]
			},
			mappedBufferCount: {
				mainStyle: "",
				title: "Mapped Buffer",
				desc: "Shows the status of agent's Mapped buffer",
				category: [{
					title: "[Legend]",
					items: [{
						name: "Mapped Buffer Count",
						desc: "Number of Mapped buffer"
					}]
				}]
			},
			mappedBufferMemory: {
				mainStyle: "",
				title: "Mapped Buffer Memory",
				desc: "Shows the status of agent's used Mapped buffer memory",
				category: [{
					title: "[Legend]",
					items: [{
						name: "Mapped Buffer Memory Used",
						desc: "Currently used Mapped buffer memory"
					}]
				}]
			},
			wrongApp: [
				"<div style='font-size:12px'>The agent is currently registered under {{application2}} due to the following:<br>",
				"1. The agent has moved from {{application1}} to {{application2}}<br>",
				"2. A different agent with the same agent id has been registered to {{application2}}<hr>",
				"Please contact the pinpoint dev. team.</div>"
			].join(""),
			statHeap: {
				mainStyle: "",
				title: "Heap",
				desc: "Heap size used by agent JVMs",
				category: [{
					title: "[Legend]",
					items: [{
						name: "MAX",
						desc: "Largest heap size used by agent JVMs"
					},{
						name: "AVG",
						desc: "Average heap size used by agent JVMs"
					},{
						name: "MIN",
						desc: "Smallest heap size used by agent JVMs"
					}]
				}]
			},
			statPermGen: {
				mainStyle: "",
				title: "Non-Heap",
				desc: "Non-heap size used by the agent JVMs",
				category: [{
					title: "[Legend]",
					items: [{
						name: "MAX",
						desc: "Largest non-heap size used by agent JVMs"
					},{
						name: "AVG",
						desc: "Average non-heap size used by agent JVMs"
					},{
						name: "MIN",
						desc: "Smallest non-heap size used by agent JVMs"
					}]
				}]
			},
			statJVMCpu: {
				mainStyle: "",
				title: "JVM Cpu Usage",
				desc: "CPU used by agent JVM processes - For multi-core CPUs, displays the average CPU usage of all the cores.",
				category: [{
					title: "[Legend]",
					items: [{
						name: "MAX",
						desc: "Largest CPU usage of agent JVM processes"
					},{
						name: "AVG",
						desc: "Average CPU usage of agent JVM processes"
					},{
						name: "MIN",
						desc: "Smallest CPU usage of agent JVM processes"
					}]
				}]
			},
			statSystemCpu: {
				mainStyle: "",
				title: "System pu Usage",
				desc: "CPU usage of agents' whole system - For multi-core CPUs, displays the average CPU usage of all the cores.",
				category: [{
					title: "[Legend]",
					items: [{
						name: "MAX",
						desc: "Largest system CPU usage of agents"
					},{
						name: "AVG",
						desc: "Average system CPU usage of agents"
					},{
						name: "MIN",
						desc: "Smallest system CPU usage of agents"
					}]
				},{
					title: "[Reference]",
					items: [{
						name: "Java 1.6",
						desc: "Only the JVM's CPU usage is collected"
					},{
						name: "Java 1.7+",
						desc: "Both the JVM's and the system's CPU usage are collected"
					}]
				}]
			},
			statTPS: {
				mainStyle: "",
				title: "TPS",
				desc: "Number of transactions received by the agents per second",
				category: [{
					title: "[Legend]",
					items: [{
						name: "MAX",
						desc: "Highest TPS of the agents"
					},{
						name: "AVG",
						desc: "Average TPS of the agents"
					},{
						name: "MIN",
						desc: "Lowest TPS of the agents"
					}]
				}]
			},
			statActiveThread: {
				mainStyle: "",
				title: "Active Thread",
				desc: "Number of active threads serving user requests",
				category: [{
					title: "[Legend]",
					items: [{
						name: "MAX",
						desc: "Highest active thread count of the agents serving user requests"
					},{
						name: "AVG",
						desc: "Average active thread count of the agents serving user requests"
					},{
						name: "MIN",
						desc: "Lowest active thread count of the agents serving user requests"
					}]
				}]
			},
			statResponseTime: {
				mainStyle: "",
				title: "Response Time",
				desc: "Average response times served by the agents",
				category: [{
					title: "[Legend]",
					items: [{
						name: "MAX",
						desc: "Highest average response time of requests served by an agent"
					},{
						name: "AVG",
						desc: "Average response time of requests served by all agents"
					},{
						name: "MIN",
						desc: "Lowest average response time of requests served by an agent"
					}]
				}]
			},
			statDataSource: {
				mainStyle: "",
				title: "Data Source",
				desc: "Status of the agents' data source",
				category: [{
					title: "[Legend]",
					items: [{
						name: "MAX",
						desc: "Largest data source connection count of the agents"
					},{
						name: "AVG",
						desc: "Average data source connection count of the agents"
					},{
						name: "MIN",
						desc: "Smallest data source connection count of the agents"
					}]
				}]
			},
			statOpenFileDescriptor: {
				mainStyle: "",
				title: "File Descriptor",
				desc: "Number of file descriptors used by agents",
				category: [{
					title: "[Legend]",
					items: [{
						name: "MAX",
						desc: "Max number of open file descriptors"
					},{
						name: "AVG",
						desc: "average open file descriptors"
					},{
						name: "MIN",
						desc: "Min number of open file descriptors"
					}]
				}]
			},
			statDirectBufferCount: {
				mainStyle: "",
				title: "Direct Buffer",
				desc: "Number of direct buffer used by agents",
				category: [{
					title: "[Legend]",
					items: [{
						name: "MAX",
						desc: "Max number of direct buffer"
					},{
						name: "AVG",
						desc: "average number of direct buffer"
					},{
						name: "MIN",
						desc: "Min number of direct buffer"
					}]
				}]
			},
			statDirectBufferMemory: {
				mainStyle: "",
				title: "Direct Buffer Memory",
				desc: "Number of Direct buffer used by agents",
				category: [{
					title: "[Legend]",
					items: [{
						name: "MAX",
						desc: "Max number of direct buffer"
					},{
						name: "AVG",
						desc: "average number of direct buffer"
					},{
						name: "MIN",
						desc: "Min number of direct buffer"
					}]
				}]
			},
			statMappedBufferCount: {
				mainStyle: "",
				title: "Mapped Buffer",
				desc: "Number of Mapped buffer used by agents",
				category: [{
					title: "[Legend]",
					items: [{
						name: "MAX",
						desc: "Max number of Mapped buffer"
					},{
						name: "AVG",
						desc: "average number of Mapped buffer"
					},{
						name: "MIN",
						desc: "Min number of Mapped buffer"
					}]
				}]
			},
			statMappedBufferMemory: {
				mainStyle: "",
				title: "Mapped Buffer Memory",
				desc: "Number of Mapped buffer used by agents",
				category: [{
					title: "[Legend]",
					items: [{
						name: "MAX",
						desc: "Max number of Mapped buffer"
					},{
						name: "AVG",
						desc: "average number of Mapped buffer"
					},{
						name: "MIN",
						desc: "Min number of Mapped buffer"
					}]
				}]
			}
		},
		callTree: {
			column: {
				mainStyle: "",
				title: "Call Tree",
				desc: "",
				category: [{
					title: "[Column]",
					items: [{
						name: "Gap",
						desc: "Time elapsed between the start of the previous method and entry of this method"
					},{
						name: "Exec",
						desc: "The overall duration of the method call from method entry until method exit"
					},{
						name: "Exec(%)",
						desc: "<img src='images/help/callTree_01.png'/>"
					},{
						name: "",
						desc: "<span style='background-color:#FFFFFF;color:#5bc0de'>Light blue</span> The execution time of the method call as a percentage of the total execution time of the transaction"
					},{
						name: "",
						desc: "<span style='background-color:#FFFFFF;color:#4343C8'>Dark blue</span> A percentage of the self execution time"
					},{
						name: "Self",
						desc: "The time that was used for execution of this method only, excluding time consumed in nested methods call"
					}]
				}]
			}
		},
		transactionTable: {
			log: {
				mainStyle: "",
				title: "NELO 로그 연동",
				desc: "연동 방법은 다음 링크를 참고하세요.<a href='http://devcafe.nhncorp.com/Pinpoint/forum/2123148' target='_blank'><span class='glyphicon glyphicon-link' aria-hidden='true' style='color:yellow'></span></a>"
			}
		},
		transactionList: {
			openError: {
				noParent: "Scatter data of parent window had been changed.\r\nso can\'t scan the data any more.",
				noData: "There is no {{application}} scatter data in parent window."
			}
		},
		applicationInspectorGuideMessage: "Application Inspector is not enabled.<br>" +
		"To enable Application Inspector, please refer to <a href='https://github.com/naver/pinpoint/blob/master/doc/application-inspector.md'>this link <span class='glyphicon glyphicon-new-window'></span></a>."
	};
	pinpointApp.constant('helpContent-en', oHelp );
})();