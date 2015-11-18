package com.navercorp.pinpoint.testweb.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URLConnection;
import java.net.URL;

/**
 * Created by Naver on 2015-11-17.
 */
@Controller
public class JdkConnectController {

    @RequestMapping(value = "/jdk/connect")
    @ResponseBody
    public String get(@RequestHeader(value = "Cookie", required = false) String cookie, HttpServletRequest request) {

        try {
            URL url = new URL("http://google.com?foo=bar");
            try {
                URLConnection connection = url.openConnection();
                connection.connect();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (MalformedURLException e) {
        }

        return "OK";
    }

}
