package com.nhn.hippo.web.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.nhn.hippo.web.service.FlowChartService;

@Controller
public class FlowChartController {

	@Autowired
	private FlowChartService flow;

	@RequestMapping(value = "/flow", method = RequestMethod.POST)
	public String arcus(Model model, @RequestParam("id") int id) {
		return "flow";
	}
}