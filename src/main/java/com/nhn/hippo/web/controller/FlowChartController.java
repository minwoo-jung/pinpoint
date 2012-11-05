package com.nhn.hippo.web.controller;

import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StopWatch;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.nhn.hippo.web.calltree.rpc.RPCCallTree;
import com.nhn.hippo.web.calltree.server.ServerCallTree;
import com.nhn.hippo.web.service.FlowChartService;
import com.nhn.hippo.web.vo.TraceId;

/**
 * retrieve data for drawing call tree.
 * 
 * @author netspider
 */
@Controller
public class FlowChartController {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private FlowChartService flow;

	@RequestMapping(value = "/flow", method = RequestMethod.GET)
	public String flow(Model model, @RequestParam("application") String applicationName, @RequestParam("from") long from, @RequestParam("to") long to) {
		String[] agentIds = flow.selectAgentIdsFromApplicationName(applicationName);
		Set<TraceId> traceIds = flow.selectTraceIdsFromTraceIndex(agentIds, from, to);

		RPCCallTree callTree = flow.selectRPCCallTree(traceIds);

		model.addAttribute("nodes", callTree.getNodes());
		model.addAttribute("links", callTree.getLinks());

		logger.debug("callTree:{}", callTree);

		return "flow";
	}

	@RequestMapping(value = "/flowserver", method = RequestMethod.GET)
	public String flowserver(Model model, @RequestParam("application") String applicationName, @RequestParam("from") long from, @RequestParam("to") long to) {
		String[] agentIds = flow.selectAgentIdsFromApplicationName(applicationName);
		// TODO 제거 하거나, interceptor로 할것.
		StopWatch watch = new StopWatch();
		watch.start("scanTraceindex");
		Set<TraceId> traceIds = flow.selectTraceIdsFromTraceIndex(agentIds, from, to);
		watch.stop();
		logger.info("time:{} {}", watch.getLastTaskTimeMillis(), traceIds.size());
		watch.start("selectServerCallTree");
		ServerCallTree callTree = flow.selectServerCallTree(traceIds);
		watch.stop();
		logger.info("time:{}", watch.getLastTaskTimeMillis());

		model.addAttribute("nodes", callTree.getNodes());
		model.addAttribute("links", callTree.getLinks());
		model.addAttribute("businessTransactions", callTree.getBusinessTransactions().getBusinessTransactionIterator());
		model.addAttribute("traces", callTree.getBusinessTransactions().getTracesIterator());

		logger.debug("callTree:{}", callTree);

		return "flowserver";
	}
}