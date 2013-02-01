package com.nhn.hippo.web.controller;

import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.nhn.hippo.web.calltree.server.ServerCallTree;
import com.nhn.hippo.web.calltree.span.SpanAlign;
import com.nhn.hippo.web.service.FlowChartService;
import com.nhn.hippo.web.service.SpanService;
import com.nhn.hippo.web.vo.TraceId;
import com.nhn.hippo.web.vo.callstacks.RecordSet;

/**
 *
 */
@Controller
public class BusinessTransactionController {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private SpanService spanService;

	@Autowired
	private FlowChartService flow;

	@RequestMapping(value = "/selectTransaction", method = RequestMethod.GET)
	public ModelAndView flow(@RequestParam(value = "traceId") String traceId) {
		logger.debug("traceId:{}", traceId);

		ModelAndView mv = new ModelAndView("selectTransaction");

		try {
			// select spans
			List<SpanAlign> spanAligns = spanService.selectSpan(traceId);

			if (spanAligns.isEmpty()) {
				mv.addObject("errorCode", 9);
				mv.setViewName("error");
				return mv;
			}

			// debug
			mv.addObject("spanList", spanAligns);

			mv.addObject("traceId", traceId);

			// call tree
			ServerCallTree callTree = flow.selectServerCallTree(new TraceId(UUID.fromString(traceId)));
			mv.addObject("nodes", callTree.getNodes());
			mv.addObject("links", callTree.getLinks());

			// call stacks
			RecordSet recordset = new RecordSet(spanAligns);
			mv.addObject("applicationName", recordset.getApplicationName());
			mv.addObject("callstack", recordset.getIterator());
			mv.addObject("timeline", recordset.getIterator());
			mv.addObject("callstackStart", recordset.getStartTime());
			mv.addObject("callstackEnd", recordset.getEndTime());

		} catch (Exception e) {
			logger.warn("BusinessTransactionController Error Cause" + e.getMessage(), e);
//             아무래도 다시 던져야 될듯한데. Exception처리 정책을 생각해봐야 한다.
//            throw e;
		}

		return mv;
	}
}
