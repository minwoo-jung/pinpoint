package com.navercorp.pinpoint.testweb.controller;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

@Controller
public class LoggingController {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final RequestMappingHandlerMapping handlerMapping;

    @Autowired
    public LoggingController(RequestMappingHandlerMapping handlerMapping) {
        this.handlerMapping = handlerMapping;
    }


    @RequestMapping(value = "/logging", method = RequestMethod.GET)
    @ResponseBody
    public String logging(Model model) {
        mdcClear();
        writeLog("logging.pinpoint api is called");
        return "OK";
    }
    
    @RequestMapping(value = "/loggingWithMDC", method = RequestMethod.GET)
    @ResponseBody
    public String logging2(Model model) {
        mdcClear();
        settingMDCValue();
        writeLog("loggingWithMDC.pinpoint api is called");
        return "OK";
    }
    
    private void mdcClear() {
        MDC.clear();
    }

    private void settingMDCValue() {
        MDC.put("time", new Date().toString());
    }


    public void writeLog(String message) {
        logger.info(message);
    }
}
