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

    @RequestMapping(value="/vertx/client/requestBody")
    @ResponseBody
    public void requestBody(final HttpServletRequest request) throws Exception {
        vertxService.request(request.getLocalPort(), request.getLocalAddr(), "/", "{foo:bar}");
    }

    @RequestMapping(value="/vertx/client/requestChunk")
    @ResponseBody
    public void requestChunk(final HttpServletRequest request) throws Exception {
        vertxService.chunk(request.getLocalPort(), request.getLocalAddr(), "/", "{foo:bar}");
    }

    @RequestMapping(value="/vertx/client/request")
    @ResponseBody
    public void request(final HttpServletRequest request) throws Exception {
        vertxService.request(request.getLocalPort(), request.getLocalAddr(), "/");
    }

    @RequestMapping(value="/vertx/client/sendHead")
    @ResponseBody
    public void sendHead(final HttpServletRequest request) throws Exception {
        vertxService.sendHead(request.getLocalPort(), request.getLocalAddr(), "/");
    }


    @RequestMapping(value="/vertx/client/request/param")
    @ResponseBody
    public void requestNaver() throws Exception {
        vertxService.request(80, "www.naver.com", "/?foo=bar");
    }

    @RequestMapping(value="/vertx/client/request/failed")
    @ResponseBody
    public void requestDaum() throws Exception {
        vertxService.request(9999, "127.0.0.1", "/");
    }

    @RequestMapping(value="/vertx/server")
    @ResponseBody
    public void server(final HttpServletRequest request) throws Exception {
        vertxService.request(VertxService.LISTEN_PORT, request.getLocalAddr(), "/");
    }

    @RequestMapping(value="/vertx/server/request")
    @ResponseBody
    public void serverRequest(final HttpServletRequest request) throws Exception {
        vertxService.request(VertxService.LISTEN_PORT, request.getLocalAddr(), "/request");
    }

    @RequestMapping(value="/vertx/server/request/param")
    @ResponseBody
    public void serverRequestParam(final HttpServletRequest request) throws Exception {
        vertxService.request(VertxService.LISTEN_PORT, request.getLocalAddr(), "/request/param?foo=bar");
    }

    @RequestMapping(value="/vertx/server/executeBlocking")
    @ResponseBody
    public void serverExecuteBlocking(final HttpServletRequest request) throws Exception {
        vertxService.request(VertxService.LISTEN_PORT, request.getLocalAddr(), "/executeBlocking");
    }

    @RequestMapping(value="/vertx/server/executeBlocking/wait3s")
    @ResponseBody
    public void serverExecuteBlockingWait3s(final HttpServletRequest request) throws Exception {
        vertxService.request(VertxService.LISTEN_PORT, request.getLocalAddr(), "/executeBlocking/wait3s");
    }

    @RequestMapping(value="/vertx/server/executeBlocking/request")
    @ResponseBody
    public void serverExecuteBlockingRequest(final HttpServletRequest request) throws Exception {
        vertxService.request(VertxService.LISTEN_PORT, request.getLocalAddr(), "/executeBlocking/request");
    }

    @RequestMapping(value="/vertx/server/runOnContext")
    @ResponseBody
    public void serverRunOnContext(final HttpServletRequest request) throws Exception {
        vertxService.request(VertxService.LISTEN_PORT, request.getLocalAddr(), "/runOnContext");
    }

    @RequestMapping(value="/vertx/server/runOnContext/wait3s")
    @ResponseBody
    public void serverRunOnContextWait3s(final HttpServletRequest request) throws Exception {
        vertxService.request(VertxService.LISTEN_PORT, request.getLocalAddr(), "/runOnContext/wait3s");
    }

    @RequestMapping(value="/vertx/server/runOnContext/request")
    @ResponseBody
    public void serverRunOnContextRequest(final HttpServletRequest request) throws Exception {
        vertxService.request(VertxService.LISTEN_PORT, request.getLocalAddr(), "/runOnContext/request");
    }

    @RequestMapping(value="/vertx/server/noresponse")
    @ResponseBody
    public void serverNoResponse(final HttpServletRequest request) throws Exception {
        vertxService.request(VertxService.LISTEN_PORT, request.getLocalAddr(), "/noresponse");
    }

    @RequestMapping(value="/vertx/server/close")
    @ResponseBody
    public void serverClose(final HttpServletRequest request) throws Exception {
        vertxService.request(VertxService.LISTEN_PORT, request.getLocalAddr(), "/close");
    }

    @RequestMapping(value="/vertx/server/connection/close")
    @ResponseBody
    public void serverConnectionClose(final HttpServletRequest request) throws Exception {
        vertxService.request(VertxService.LISTEN_PORT, request.getLocalAddr(), "/connection/close");
    }

    private String getLocalAddress(final HttpServletRequest request) {
        return request.getLocalAddr() + ":" + request.getLocalPort();
    }
}