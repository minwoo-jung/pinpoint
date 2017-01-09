package com.navercorp.test.pinpoint.testweb.controller;

import com.navercorp.test.pinpoint.testweb.service.VertxService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;

/**
 * @author jaehong.kim
 */
@Controller
public class VertxController {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private VertxService vertxService;

    @RequestMapping(value="/vertx/request")
    @ResponseBody
    public void request(final HttpServletRequest request) throws Exception {
        vertxService.request(request.getLocalPort(), request.getLocalAddr(), "/");
    }

    @RequestMapping(value="/vertx/request/param")
    @ResponseBody
    public void requestNaver() throws Exception {
        vertxService.request(80, "www.naver.com", "/?foo=bar");
    }

    @RequestMapping(value="/vertx/request/failed")
    @ResponseBody
    public void requestDaum() throws Exception {
        vertxService.request(9999, "127.0.0.1", "/");
    }

    private String getLocalAddress(final HttpServletRequest request) {
        return request.getLocalAddr() + ":" + request.getLocalPort();
    }
}