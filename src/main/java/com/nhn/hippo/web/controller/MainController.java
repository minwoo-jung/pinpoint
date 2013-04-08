package com.nhn.hippo.web.controller;

import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.nhn.hippo.web.service.FlowChartService;
import com.nhn.hippo.web.service.MonitorService;
import com.nhn.hippo.web.vo.Application;
import com.profiler.common.bo.AgentInfoBo;

/**
 * 
 * @author netspider
 */
@Controller
public class MainController extends BaseController {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private FlowChartService flow;

	@Autowired
	private MonitorService monitor;

	@RequestMapping(value = "/applications", method = RequestMethod.GET)
	public String flow(Model model, HttpServletResponse response) {
		List<Application> applications = flow.selectAllApplicationNames();
		model.addAttribute("applications", applications);

		logger.debug("Applications, {}", applications);

		addResponseHeader(response);
		return "applications";
	}

	@RequestMapping(value = "/agentStatus", method = RequestMethod.GET)
	public String agentStatus(Model model, HttpServletResponse response, @RequestParam("agentId") String agentId) {
		AgentInfoBo agentInfo = monitor.getAgentInfo(agentId);

		long gap = System.currentTimeMillis() - agentInfo.getTimestamp();
		
		model.addAttribute("gap", gap);
		model.addAttribute("agentinfo", agentInfo);

		addResponseHeader(response);
		return "agentstatus";
	}
}