function drawSankeyChart(graphdata, targetId) {
	var margin = {
		    top:1,
		    right:1,
		    bottom:6,
		    left:1
		}, width = 1300 - margin.left - margin.right, height = 450 - margin.top - margin.bottom;

	var formatNumber = d3.format(",.0f"), format = function (d) {
	    	return formatNumber(d) + " Requests";
		}, color = d3.scale.category20();

    var svg = d3.select(targetId).append("svg")
            .attr("width", width + margin.left + margin.right)
            .attr("height", height + margin.top + margin.bottom)
            .append("g").attr("transform", "translate(" + margin.left + "," + margin.top + ")");
    var sankey = d3.sankey().nodeWidth(15).nodePadding(10).size([ width, height ]);
    var path = sankey.link();

	sankey.nodes(graphdata.nodes).links(graphdata.links).layout(32);
	
	if (graphdata.nodes.length == 0) {
	    alert("no data");
	    return;
	}
	
	var link = svg.append("g").selectAll(".link").data(graphdata.links)
	        .enter().append("path")
	        .attr("class", "link")
	        .attr("d", path).style("stroke-width",function (d) {
	            return Math.max(1, d.dy);
	        }).sort(function (a, b) {
	            return b.dy - a.dy;
	        }).on("click", function (a, b) {
	            console.log(a);
	            console.log(b);
	        });
	
	link.append("title").text(
	        function (d) {
	            return d.source.name + " → " + d.target.name + "\n"
	                    + format(d.value);
	        });
	
	var node = svg.append("g").selectAll(".node").data(graphdata.nodes)
	        .enter().append("g").attr("class", "node").attr(
	        "transform",function (d) {
	            return "translate(" + d.x + "," + d.y + ")";
	        }).call(d3.behavior.drag().origin(function (d) {
	    return d;
	}).on("dragstart",function () {
	            this.parentNode.appendChild(this);
	        }).on("drag", dragmove));
	
	node.on("click", function (d) {
	    console.log(d);
	});
	
	node.append("rect").attr("height",function (d) {
	    return d.dy;
	}).attr("width", sankey.nodeWidth()).style("fill",function (d) {
	            return d.color = color(d.name.replace(/ .*/, ""));
	        }).style("stroke",function (d) {
	            return d3.rgb(d.color).darker(2);
	        }).append("title").text(function (d) {
	            return d.name + "\n" + format(d.value);
	        });
	
	node.append("text").attr("x", -6).attr("y",function (d) {
	    return d.dy / 2;
	}).attr("dy", ".35em").attr("text-anchor", "end").attr("transform",
	        null).text(function (d) {
	            return d.name;
	        }).filter(function (d) {
	            return d.x < width / 2;
	        }).attr("x", 6 + sankey.nodeWidth()).attr("text-anchor", "start");
	
	function dragmove(d) {
	    d3.select(this).attr(
	            "transform",
	            "translate("
	                    + d.x
	                    + ","
	                    + (d.y = Math.max(0, Math.min(height - d.dy,
	                    d3.event.y))) + ")");
	    sankey.relayout();
	    link.attr("d", path);
	}
}

function drawScatter(data, targetId) {
	var margin = {top: 20, right: 20, bottom: 30, left: 40},
	    width = 960 - margin.left - margin.right,
	    height = 500 - margin.top - margin.bottom;

	var x = d3.time.scale().range([0, width]);
	var y = d3.scale.linear().range([height, 0]);
	var color = d3.scale.category10();

	var xAxis = d3.svg.axis()
	    .scale(x)
	    .orient("bottom");

	var yAxis = d3.svg.axis()
	    .scale(y)
	    .orient("left");
	
	var svg = d3.select(targetId).append("svg")
	    .attr("width", width + margin.left + margin.right)
	    .attr("height", height + margin.top + margin.bottom)
	  .append("g")
	    .attr("transform", "translate(" + margin.left + "," + margin.top + ")");

	  //data.forEach(function(d) {
	  //  d.timestamp = +d.timestamp;
	  //  d.executionTime = +d.executionTime;
	  //});

	  x.domain(d3.extent(data, function(d) { return d.timestamp; }));//.nice();
	  y.domain(d3.extent(data, function(d) { return d.executionTime; }));//.nice();

	  svg.append("g")
	      .attr("class", "x axis")
	      .attr("transform", "translate(0," + height + ")")
	      .call(xAxis)
	    .append("text")
	      .attr("class", "label")
	      .attr("x", width)
	      .attr("y", -6)
	      .style("text-anchor", "end")
	      .text("Timestamp (HH:mm)");

	  svg.append("g")
	      .attr("class", "y axis")
	      .call(yAxis)
	    .append("text")
	      .attr("class", "label")
	      .attr("transform", "rotate(-90)")
	      .attr("y", 6)
	      .attr("dy", ".71em")
	      .style("text-anchor", "end")
	      .text("Execute time (ms)")

	  svg.selectAll(".dot")
	      .data(data)
	    .enter().append("circle")
	      .attr("class", "dot")
	      .attr("r", 3.5)
	      .attr("cx", function(d) { return x(d.timestamp); })
	      .attr("cy", function(d) { return y(d.executionTime); })
	      .style("fill", function(d) { return color(d.name); })
	      .on("click", function(d) { openTrace(d.traceId); });

	  var legend = svg.selectAll(".legend")
	      .data(color.domain())
	    .enter().append("g")
	      .attr("class", "legend")
	      .attr("transform", function(d, i) { return "translate(0," + i * 20 + ")"; });

	  legend.append("rect")
	      .attr("x", width - 18)
	      .attr("width", 18)
	      .attr("height", 18)
	      .style("fill", color);

	  legend.append("text")
	      .attr("x", width - 24)
	      .attr("y", 9)
	      .attr("dy", ".35em")
	      .style("text-anchor", "end")
	      .text(function(d) { return d; });
}

function openTrace(uuid) {
    window.open("/selectTransaction.hippo?traceId=" + uuid);
}