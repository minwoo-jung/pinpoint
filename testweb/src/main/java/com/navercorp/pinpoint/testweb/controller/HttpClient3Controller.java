package com.navercorp.pinpoint.testweb.controller;

import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.navercorp.pinpoint.testweb.connector.apachehttp3.ApacheHttpClient3;

@Controller
public class HttpClient3Controller {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @RequestMapping(value = "/httpclient3/get")
    @ResponseBody
    public String get(@RequestHeader(value = "Cookie", required = false) String cookie, HttpServletRequest request) {
        ApacheHttpClient3 client = new ApacheHttpClient3();
        client.executeGet("http://www.naver.com", new HashMap<String, Object>(), null);
        
        return "OK";
    }
    
    @RequestMapping(value = "/httpclient3/cookie")
    @ResponseBody
    public String cookie(@RequestHeader(value = "Cookie", required = false) String cookie, HttpServletRequest request) {
        logger.info("Cookie:{}", cookie);

        ApacheHttpClient3 client = new ApacheHttpClient3();
        client.executeWithCookie("http://www.naver.com", new HashMap<String, Object>(), null);

        return "OK";
    }
    
    @RequestMapping(value = "/httpclient3/post")
    @ResponseBody
    public String post(@RequestHeader(value = "Cookie", required = false) String cookie, HttpServletRequest request) {
        logger.info("Cookie:{}", cookie);
        
        ApacheHttpClient3 client = new ApacheHttpClient3();
        client.executePost();
        
        return "OK";
    }
    
    
    @RequestMapping(value = "/httpclient3/remoteCall")
    @ResponseBody
    public String remoteCall(@RequestHeader(value = "Cookie", required = false) String cookie, HttpServletRequest request) {
        logger.info("Cookie:{}", cookie);

        ApacheHttpClient3 client = new ApacheHttpClient3();
        client.executeWithCookie("http://localhost:8083/nhnent/get.pinpoint", new HashMap<String, Object>(), null);

        return "OK";
    }
    
}
