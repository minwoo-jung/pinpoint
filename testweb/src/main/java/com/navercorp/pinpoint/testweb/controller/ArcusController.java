package com.navercorp.pinpoint.testweb.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.navercorp.pinpoint.testweb.service.ArcusService;

@Controller
public class ArcusController {

    @Autowired
    private ArcusService arcusService;

    @RequestMapping(value = "/arcus/get")
    @ResponseBody
    public String get() {
        arcusService.get();
        return "OK";
    }

    @RequestMapping(value = "/arcus/set")
    @ResponseBody
    public String set() {
        arcusService.set();
        return "OK";
    }

    @RequestMapping(value = "/arcus/delete")
    @ResponseBody
    public String delete() {
        arcusService.delete();
        return "OK";
    }

    @RequestMapping(value = "/arcus/timeout")
    @ResponseBody
    public String timeout() {
        arcusService.timeout();
        return "OK";
    }
}
