<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<meta charset="utf-8">
<title>Sankey Diagram</title>
<style>
    @import url(style.css);

    #chart {
        height: 500px;
    }

    .node rect {
        cursor: move;
        fill-opacity: .9;
        shape-rendering: crispEdges;
    }

    .node text {
        pointer-events: none;
        text-shadow: 0 1px 0 #fff;
    }

    .link {
        fill: none;
        stroke: #000;
        stroke-opacity: .2;
    }

    .linkOver {
        stroke-opacity: .5;
    }

    .link:hover {
        stroke-opacity: .5;
    }
</style>
<body>
<h1>HIPPO flow chart (DRAFT)</h1>

<p id="chart"></p>

<script src="http://www.stathat.com/javascripts/embed.js"></script>
<script>StatHatEmbed.render({s1:'OuDfu', w:760, h:235});</script>
<a href="http://www.stathat.com/stats/OuDfu">Analyze on StatHat</a>


<script src="http://d3js.org/d3.v2.min.js?2.9.1"></script>
<script src="sankey.js"></script>
<script>
    var margin = {
        top:1,
        right:1,
        bottom:6,
        left:1
    }, width = 800 - margin.left - margin.right, height = 300 - margin.top - margin.bottom;

    var formatNumber = d3.format(",.0f"), format = function (d) {
        return formatNumber(d) + " Requests";
    }, color = d3.scale.category20();

    var svg = d3.select("#chart").append("svg")
            .attr("width", width + margin.left + margin.right)
            .attr("height", height + margin.top + margin.bottom)
            .append("g").attr("transform", "translate(" + margin.left + "," + margin.top + ")");

    var sankey = d3.sankey().nodeWidth(15).nodePadding(10).size([ width, height ]);

    var path = sankey.link();

    d3.json("data.json", function (energy) {
        sankey.nodes(energy.nodes).links(energy.links).layout(32);

        /*
         LINKS
         */
        var link = svg.append("g").selectAll(".link").data(energy.links)
                .enter().append("path")
                .attr("class", "link")
                .attr("d", path).style("stroke-width",function (d) {
                    return Math.max(1, d.dy);
                }).sort(function (a, b) {
                    return b.dy - a.dy;
                }).on("click", function (a, b) {
                    console.log("[LINK CLICK] Get request details here.");
                    console.log(a);
                    console.log(b);
                });

        link.append("title").text(
                function (d) {
                    return d.source.name + " → " + d.target.name + "\n"
                            + format(d.value);
                });

        /*
         NODES
         */
        var node = svg.append("g").selectAll(".node").data(energy.nodes)
                .enter().append("g").attr("class", "node").attr(
                "transform",function (d) {
                    return "translate(" + d.x + "," + d.y + ")";
                }).call(d3.behavior.drag().origin(function (d) {
            return d;
        }).on("dragstart",function () {
                    this.parentNode.appendChild(this);
                }).on("drag", dragmove));

        node.on("mouseover",function (d) {
            //console.log(d.name);

            //console.log(d3.selectAll(".link"));

            //console.log(d3.selectAll(".link")[0][0]);

            console.log(d3.selectAll("#chart .link").select().attr("class", ""));

            console.log("mouse over");
        }).on("mouseout", function (d) {

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
    });
</script>