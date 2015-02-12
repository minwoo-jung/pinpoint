package com.navercorp.pinpoint.testweb.controller;

import com.navercorp.pinpoint.testweb.service.NimmService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author netspider
 */
@Controller
public class NIMMController {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private NimmService nimmService;

    @RequestMapping(value = "/nimm/invokeAndReturnValue")
    @ResponseBody
    public String invokeAndReturnValue() {
        nimmService.get(null);
        return "OK";
    }

    @RequestMapping(value = "/nimm/invokeAndCallback")
    @ResponseBody
    public String invokeAndCallabck() {
        Runnable callback = new Runnable() {
            public void run() {
                // TODO
            }
        };

        nimmService.get(callback);
        return "OK";
    }
}
