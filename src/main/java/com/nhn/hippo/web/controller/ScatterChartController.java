package com.nhn.hippo.web.controller;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StopWatch;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.nhn.hippo.web.filter.Filter;
import com.nhn.hippo.web.filter.FilterBuilder;
import com.nhn.hippo.web.service.ScatterChartService;
import com.nhn.hippo.web.vo.TransactionMetadataQuery;
import com.nhn.hippo.web.vo.scatter.Dot;
import com.profiler.common.bo.SpanBo;

/**
 * 
 * @author netspider
 */
@Controller
public class ScatterChartController extends BaseController {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private ScatterChartService scatter;

	@RequestMapping(value = "/selectedScatter", method = RequestMethod.GET)
	public String selectedScatter(Model model, HttpServletResponse response) {
		return "selectedScatter";
	}
	
	@RequestMapping(value = "/selectedScatterList", method = RequestMethod.GET)
	public String selectedScatterList(Model model, HttpServletResponse response) {
		return "selectedScatterList";
	}
	
	@RequestMapping(value = "/scatterpopup", method = RequestMethod.GET)
	public String scatterPopup(Model model, HttpServletResponse response, @RequestParam("application") String applicationName, @RequestParam("from") long from, @RequestParam("to") long to, @RequestParam("period") long period, @RequestParam("usePeriod") boolean usePeriod, @RequestParam(value = "filter", required = false) String filterText) {
		model.addAttribute("applicationName", applicationName);
		model.addAttribute("from", from);
		model.addAttribute("to", to);
		model.addAttribute("period", period);
		model.addAttribute("usePeriod", usePeriod);
		model.addAttribute("filter", filterText);
		return "scatterPopup";
	}

	/**
	 * 
	 * @param model
	 * @param response
	 * @param applicationName
	 * @param from
	 * @param to
	 * @param limit
	 *            한번에 조회 할 데이터의 크기, 조회 결과가 이 크기를 넘어가면 limit개만 반환한다. 나머지는 다시 요청해서
	 *            조회해야 한다.
	 * @return
	 */
	@RequestMapping(value = "/getScatterData", method = RequestMethod.GET)
	public String getScatterData(Model model, HttpServletResponse response, @RequestParam("application") String applicationName, @RequestParam("from") long from, @RequestParam("to") long to, @RequestParam("limit") int limit, @RequestParam(value = "filter", required = false) String filterText, @RequestParam(value = "_callback", required = false) String jsonpCallback) {
		StopWatch watch = new StopWatch();
		watch.start("selectScatterData");

		Filter filter = FilterBuilder.build(filterText);
		List<Dot> scatterData = scatter.selectScatterData(applicationName, from, to, limit, filter);
		watch.stop();

		logger.info("Fetch scatterData time : {}ms", watch.getLastTaskTimeMillis());

		model.addAttribute("scatter", scatterData);

		addResponseHeader(response);

		if (jsonpCallback == null) {
			return "scatter_json";
		} else {
			model.addAttribute("callback", jsonpCallback);
			return "scatter_jsonp";
		}
	}

	/**
	 * NOW 버튼을 눌렀을 때 scatter 데이터 조회.
	 * 
	 * @param model
	 * @param response
	 * @param applicationName
	 * @param from
	 * @param limit
	 * @return
	 */
	@RequestMapping(value = "/getLastScatterData", method = RequestMethod.GET)
	public String getLastScatterData(Model model, HttpServletResponse response, @RequestParam("application") String applicationName, @RequestParam("period") long period, @RequestParam("limit") int limit, @RequestParam(value = "filter", required = false) String filterText, @RequestParam(value = "_callback", required = false) String jsonpCallback) {
		long to = getQueryEndTime();
		long from = to - period;
		return getScatterData(model, response, applicationName, from, to, limit, filterText, jsonpCallback);
	}

	/**
	 * scatter 실시간 갱신에서는 to 시간을 지정하지 않는다. server time을 사용하고 조회된 시간 범위를 반환해준다.
	 * UI에서는 반환된 조회 범위를 참조해서 다음 쿼리를 요청한다.
	 * 
	 * @param model
	 * @param response
	 * @param applicationName
	 * @param from
	 * @param to
	 * @param limit
	 * @return
	 */
	@RequestMapping(value = "/getRealtimeScatterData", method = RequestMethod.GET)
	public String getRealtimeScatterData(Model model, HttpServletResponse response, @RequestParam("application") String applicationName, @RequestParam("from") long from, @RequestParam("limit") int limit) {
		StopWatch watch = new StopWatch();
		watch.start("selectScatterData");

		long to = getQueryEndTime();

		// TODO need filter??
		List<Dot> scatterData = scatter.selectScatterData(applicationName, from, to, limit, Filter.NONE);
		watch.stop();

		logger.info("Fetch scatterData time : {}ms", watch.getLastTaskTimeMillis());

		model.addAttribute("scatter", scatterData);
		model.addAttribute("queryFrom", from);

		if (scatterData.size() >= limit) {
			model.addAttribute("queryTo", scatterData.get(scatterData.size() - 1).getTimestamp());
		} else {
			model.addAttribute("queryTo", to);
		}

		model.addAttribute("limit", limit);

		addResponseHeader(response);
		return "scatterRealtime";
	}

	/**
	 * scatter에서 점 여러개를 선택했을 때 점에 대한 정보를 조회한다.
	 * 
	 * @param model
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/transactionmetadata", method = RequestMethod.POST)
	public String transactionmetadata(Model model, HttpServletRequest request, HttpServletResponse response) {
		String TRACEID = "tr";
		String TIME = "ti";
		String RESPONSE_TIME = "re";

		TransactionMetadataQuery query = new TransactionMetadataQuery();

		int index = 0;
		while (true) {
			String traceId = request.getParameter(TRACEID + index);
			String time = request.getParameter(TIME + index);
			String responseTime = request.getParameter(RESPONSE_TIME + index);

			if (traceId == null || time == null || responseTime == null) {
				break;
			}

			query.addQueryCondition(traceId, Long.parseLong(time), Integer.parseInt(responseTime));
			index++;
		}

		if (query.size() > 0) {
			List<SpanBo> metadata = scatter.selectTransactionMetadata(query);
			model.addAttribute("metadata", metadata);
		}

		addResponseHeader(response);
		return "transactionmetadata";
	}
}