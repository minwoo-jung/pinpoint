package com.nhn.pinpoint.web.controller;

import java.util.Date;
import java.util.Set;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.nhn.pinpoint.web.applicationmap.ApplicationMap;
import com.nhn.pinpoint.web.calltree.server.ServerCallTree;
import com.nhn.pinpoint.web.filter.Filter;
import com.nhn.pinpoint.web.filter.FilterBuilder;
import com.nhn.pinpoint.web.service.ApplicationMapService;
import com.nhn.pinpoint.web.service.FlowChartService;
import com.nhn.pinpoint.web.util.TimeUtils;
import com.nhn.pinpoint.web.vo.LinkStatistics;
import com.nhn.pinpoint.web.vo.TraceId;
import com.nhn.pinpoint.common.ServiceType;

/**
 * 
 * @author netspider
 */
@Controller
public class ApplicationMapController {

	@Autowired
	private ApplicationMapService applicationMapService;

	@Autowired
	private FlowChartService flow;

	@RequestMapping(value = "/getServerMapData2", method = RequestMethod.GET)
	public String getServerMapData2(Model model,
									HttpServletResponse response,
									@RequestParam("application") String applicationName, 
									@RequestParam("serviceType") short serviceType, 
									@RequestParam("from") long from,
									@RequestParam("to") long to,
									@RequestParam(value = "hideIndirectAccess", defaultValue = "false") boolean hideIndirectAccess) {
		
		ApplicationMap map = applicationMapService.selectApplicationMap(applicationName, serviceType, from, to, hideIndirectAccess);

		model.addAttribute("nodes", map.getNodes());
		model.addAttribute("links", map.getLinks());

		return "applicationmap";
	}

	@RequestMapping(value = "/getLastServerMapData2", method = RequestMethod.GET)
	public String getLastServerMapData2(Model model,
										HttpServletResponse response,
										@RequestParam("application") String applicationName,
										@RequestParam("serviceType") short serviceType,
										@RequestParam("period") long period,
										@RequestParam(value = "hideIndirectAccess", defaultValue = "false") boolean hideIndirectAccess) {
		
		long to = TimeUtils.getDelayLastTime();
		long from = to - period;
		return getServerMapData2(model, response, applicationName, serviceType, from, to, hideIndirectAccess);
	}

	@RequestMapping(value = "/filtermap", method = RequestMethod.GET)
	public String filtermap(Model model,
							HttpServletResponse response,
							@RequestParam("application") String applicationName, 
							@RequestParam("serviceType") short serviceType,
							@RequestParam("from") long from,
							@RequestParam("to") long to, 
							@RequestParam(value = "filter", required = false) String filterText) {
		
		model.addAttribute("applicationName", applicationName);
		model.addAttribute("serviceType", serviceType);
		model.addAttribute("from", from);
		model.addAttribute("to", to);
		model.addAttribute("fromDate", new Date(from));
		model.addAttribute("toDate", new Date(to));
		model.addAttribute("filterText", filterText);
		model.addAttribute("filter", FilterBuilder.build(filterText));

		return "applicationmap.filtered.view";
	}

	@RequestMapping(value = "/getFilteredServerMapData", method = RequestMethod.GET)
	public String getFilteredServerMapData(Model model,
											HttpServletResponse response,
											@RequestParam("application") String applicationName, 
											@RequestParam("serviceType") short serviceType,
											@RequestParam("from") long from,
											@RequestParam("to") long to,
											@RequestParam(value = "filter", required = false) String filterText) {
		Set<TraceId> traceIdSet = flow.selectTraceIdsFromApplicationTraceIndex(applicationName, from, to);
		Filter filter = FilterBuilder.build(filterText);
		ServerCallTree map = flow.selectServerCallTree(traceIdSet, filter);
		
		model.addAttribute("nodes", map.getNodes());
		model.addAttribute("links", map.getLinks());
		model.addAttribute("filter", filter);

		return "applicationmap.filtered";
	}
	
	// 선택한 연결선을 통과하는 요청의 통계 정보 조회.
	// 필터 사용 안함.
	@RequestMapping(value = "/linkStatistics", method = RequestMethod.GET)
	public String getLinkStatistics(Model model,
			HttpServletResponse response, 
			@RequestParam("from") long from,
			@RequestParam("to") long to,
			@RequestParam("srcApplicationName") String srcApplicationName,
			@RequestParam("srcServiceType") short srcServiceType,
			@RequestParam("destApplicationName") String destApplicationName,
			@RequestParam("destServiceType") short destServiceType) {
		
		LinkStatistics linkStatistics = flow.linkStatistics(from, to, srcApplicationName, srcServiceType, destApplicationName, destServiceType);

		model.addAttribute("from", from);
		model.addAttribute("to", to);

		model.addAttribute("srcApplicationName", srcApplicationName);
		model.addAttribute("destApplicationName", destApplicationName);

		model.addAttribute("srcApplicationType", ServiceType.findServiceType(srcServiceType));
		model.addAttribute("destApplicationType", ServiceType.findServiceType(destServiceType));

		model.addAttribute("linkStatistics", linkStatistics);
		model.addAttribute("histogramSummary", linkStatistics.getHistogramSummary().entrySet().iterator());
		model.addAttribute("timeseriesSlotIndex", linkStatistics.getTimeseriesSlotIndex().entrySet().iterator());
		model.addAttribute("timeseriesValue", linkStatistics.getTimeseriesValue());
		model.addAttribute("timeseriesSuccessHistogram", linkStatistics.getTimeseriesFaileureHistogram().entrySet().iterator());
		model.addAttribute("timeseriesFaileureHistogram", linkStatistics.getTimeseriesFaileureHistogram().entrySet().iterator());

		return "linkStatisticsDetail";
	}
	
	// 선택한 연결선을 통과하는 요청의 통계 정보 조회.
	// 필터 사용.
	@RequestMapping(value = "/filteredLinkStatistics", method = RequestMethod.GET)
	public String getLinkStatisticsDetail(Model model,
									HttpServletResponse response, 
									@RequestParam("application") String applicationName,
									@RequestParam("serviceType") short serviceType,
									@RequestParam("from") long from,
									@RequestParam("to") long to,
									@RequestParam("srcApplicationName") String srcApplicationName,
									@RequestParam("srcServiceType") short srcServiceType,
									@RequestParam("destApplicationName") String destApplicationName,
									@RequestParam("destServiceType") short destServiceType,
									@RequestParam(value = "filter", required = false) String filterText) {
		
		Set<TraceId> traceIdSet = flow.selectTraceIdsFromApplicationTraceIndex(applicationName, from, to);
		Filter filter = FilterBuilder.build(filterText);
		LinkStatistics linkStatistics = flow.linkStatisticsDetail(traceIdSet, srcApplicationName, srcServiceType, destApplicationName, destServiceType, filter);
		
		model.addAttribute("linkStatistics", linkStatistics);
		
		return "linkStatisticsDetail";
	}
}