package com.navercorp.pinpoint.testweb.controller;

import com.navercorp.pinpoint.testweb.connector.apachehttp4.nhnent.HttpUtil;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author netspider
 */
@Controller
public class BLOC4Controller {

    private final String LOCAL_BLOC4_FORMAT = "http://%s:%s/welcome/test/hello?name=netspider";

    @RequestMapping(value = "/bloc4/callLocal")
    @ResponseBody
    public String requestGet(
            @RequestParam(required = false, defaultValue = "localhost") String host,
            @RequestParam(required = false, defaultValue = "5001") String port) {
        return HttpUtil.url(String.format(LOCAL_BLOC4_FORMAT, host, port)).method(HttpUtil.Method.GET).connectionTimeout(10000).readTimeout(10000).getContents();
    }
}
