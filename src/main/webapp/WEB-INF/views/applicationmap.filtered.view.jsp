<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="pinpoint" uri="http://pinpoint.nhncorp.com/pinpoint" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <title>PINPOINT</title>
    <meta charset="utf-8">
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
    <meta name="viewport" content="width=device-width, initial-scale=1.0">

    <link href="/components/bootstrap/css/bootstrap.css" rel="stylesheet">
    <link href="/components/bootstrap/css/bootstrap-responsive.css" rel="stylesheet"/>
    <link href="/components/pinpoint/css/pinpoint.css" rel="stylesheet"/>
    <link href="/components/pinpoint-scatter/css/scatter.css" rel="stylesheet"/>
    <link href="/components/bootstrap-datepicker/bootstrap-datepicker.css" rel="stylesheet"/>
    <link href="/components/nvd3/nv.d3.css" rel="stylesheet"/>
    <link href="/components/select2/select2.css" rel="stylesheet"/>

    <!-- Le HTML5 shim, for IE6-8 support of HTML5 elements -->
    <!--[if lt IE 9]>
    <script src="http://html5shim.googlecode.com/svn/trunk/html5.js"></script>
    <![endif]-->

	<!-- commons -->    
    <script type="text/javascript" src="/components/jquery/jquery-1.9.1.min.js"></script>
    <script type="text/javascript" src="/components/jquery-ui/jquery-ui-1.10.2.js"></script>
	<script type="text/javascript" src="/components/jquery-class/jquery.Class.js"></script>
    <script type="text/javascript" src="/components/jquery-template/jquery.tmpl.min.js"></script>
    <script type="text/javascript" src="/components/bootstrap/js/bootstrap.min.js"></script>
	<script type="text/javascript" src="/components/bootstrap-datepicker/bootstrap-datepicker.js"></script>
	<script type="text/javascript" src="/components/modernizer/modernizr-2.6.2.min.js"></script>
	<script type="text/javascript" src="/components/underscore/underscore-min.js"></script>
    <script type="text/javascript" src="/components/select2/select2.js"></script>
    <script type="text/javascript" src="/components/utils/date.js"></script>
    <script type="text/javascript" src="/components/pinpoint/pinpoint.js"></script>
    <script type="text/javascript" src="/components/pinpoint/navigationbar.js"></script>
    
    <!-- scatter chart -->
	<script type="text/javascript" src="/components/jquery-dragtoselect/jquery.dragToSelect.js"></script>
	<script type="text/javascript" src="/components/pinpoint-scatter/jquery.BigScatterChart.js"></script>
    <script type="text/javascript" src="/components/pinpoint/chart-scatter4.js"></script>
    
	<!-- server map -->
    <script type="text/javascript" src="/components/gojs/go.js"></script>
    <script type="text/javascript" src="/components/pinpoint-servermap/Point2D.js"></script>
    <script type="text/javascript" src="/components/pinpoint-servermap/intersection.js"></script>
    <script type="text/javascript" src="/components/pinpoint-servermap/canvas.roundRect.js"></script>
    <script type="text/javascript" src="/components/pinpoint-servermap/jquery.ServerMap.js"></script>
    <script type="text/javascript" src="/components/pinpoint/chart-servermap.js"></script>
    
    <!-- link info chart -->
    <script type="text/javascript" src="/components/d3/d3.v2.min.js"></script>
    <script type="text/javascript" src="/components/nvd3/nv.d3.js"></script>
    <script type="text/javascript" src="/components/pinpoint/chart-stacked.js"></script>
    
    <!-- requests list -->
    <script type="text/javascript" src="/components/pinpoint/chart-transactions.js"></script>
    
    <!-- help -->
    <script type="text/javascript" src="/components/pinpoint/help.js"></script>
    <script type="text/javascript" src="/components/pinpoint/message.js"></script>
</head>
<body>


<div class="navbar navbar-fixed-top">
  <div class="navbar-inner">
    <div class="container">
      <button type="button" class="btn btn-navbar" data-toggle="collapse" data-target=".nav-collapse">
        <span class="icon-bar"></span>
        <span class="icon-bar"></span>
        <span class="icon-bar"></span>
      </button>
      <img class="brand" src="/images/logo.png" width="116" height="18" />
      <div class="nav-collapse collapse">
        <ul class="nav">
          <li class="">
	      	<p class="navbar-text">Filtered Map</p>
          </li>
          <li class="divider-vertical"></li>
          <li class="">
          	<p class="navbar-text">${applicationName}</p>
          </li>
          <li class="divider-vertical"></li>
          <li class="">
          	<p class="navbar-text">
	      	<fmt:formatDate value="${fromDate}" pattern="yyyy-MM-dd HH:mm:ss"/> ~ <fmt:formatDate value="${toDate}" pattern="yyyy-MM-dd HH:mm:ss"/>
	      	</p>
          </li>
          <li class="divider-vertical"></li>
          <li class="">
          	<p class="navbar-text">
          		${filter}
          	</p>
          </li>
          <li class="divider-vertical"></li>
          <li class="">
			<div class="btn-group">
			  <a class="btn btn-mini dropdown-toggle" data-toggle="dropdown" href="#">
			    options
			    <span class="caret"></span>
			  </a>
			  <ul class="dropdown-menu">
				<li><a href="#" id="mergeUnknown" data-selected="true"><i class="icon-ok"></i> Merge unknowns</a></li>
			  </ul>
			</div>
          </li>
        </ul>
      </div>
    </div>
  </div>
</div>

<div class="container" id="warningMessage"></div>

<!-- BODY -->
<div class="chart-container">
    <div class="chart-left">
    	<div class="container">
    		<!--
	    	<div class="alert alert-info">
	    		<button type="button" class="close" data-dismiss="alert">&times;</button>
	    		Filter : ${filterText}
	    	</div>
			-->
			<div id="progressbar"><img src="/images/ajaxloader.gif" /></div>
    	</div>
		<div id="servermap" class="servermap"></div>
	</div>
	<div class="chart-right">
		<div id="scatterChartContainer" style="display:none;">
			<span onclick="" style="text-decoration:underline;cursor:pointer">Show all transactions</span>
			&nbsp;&nbsp;&nbsp;
			<i class="icon-fullscreen" onclick="expandScatter($(this));" onmouseover="$(this).tooltip('show');" title="Expand to new popup window." style="cursor:pointer;"></i>
			&nbsp;&nbsp;&nbsp;
			<a href=""><i class="icon-download-alt" onmouseover="$(this).tooltip('show');" title="Download as PNG image format file." style="cursor:pointer;"></i></a>
			<div id="scatterchart"></div>
		</div>
		<div id="statisticsProgressbar" style="display:none;"><img src="/images/ajaxloader.gif" /></div>
		<div id="nodeInfoDetails">
			<div class='info'></div>
		</div>
		<div id="linkInfoDetails">
			<div class='info'></div>
			<div class='linkInfoChart' style='width:100%;display:none'>
				Response histogram (UNDERCONSTRUCTION)
				<svg style='height:200px' />
			</div>
			<div class='linkInfoBarChart' style='width:100%;display:none'>
				Response histogram summary (UNDERCONSTRUCTION)
				<svg style='height:150px' />
			</div>
			<div class='linkInfoSFChart' style='width:100%;display:none'>
				Failure rate (UNDERCONSTRUCTION)
				<svg style='height:150px' />
			</div>
		</div>
	</div>
</div>
<!-- END OF BODY -->

<script type="text/javascript">
$(document).ready(function () {
	$("#mergeUnknown").bind('click', toggleMergeUnknowns);
	
	showServerMap("${applicationName}", "${serviceType}", ${from}, ${to}, 0, false, "${filterText}", false, function() {
		$("#progressbar").hide();
	});
	
	showResponseScatter("${applicationName}", ${from}, ${to}, 0, false, "${filterText}");
});
</script>
<script id="NodeInfoBox" type="text/x-jquery-tmpl">
	<div class="NodeInfoBox">NodeInfoBox
	</div>
</script>
<script id="LinkInfoBox" type="text/x-jquery-tmpl">
	<div class="LinkInfoBox">LinkInfoBox
	</div>
</script>

<script id="UnknownNodeInfoBox" type="text/x-jquery-tmpl">
	<div class="UnknownNodeInfoBox">UnknownNodeInfoBox
		 
	</div>
</script>
<script id="UnknownLinkInfoBox" type="text/x-jquery-tmpl">
	<div class="UnknownLinkInfoBox">UnknownLinkInfoBox
	 
	</div>
</script>
</body>
</html>