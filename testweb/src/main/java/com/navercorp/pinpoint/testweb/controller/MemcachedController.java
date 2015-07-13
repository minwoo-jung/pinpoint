package com.navercorp.pinpoint.testweb.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.navercorp.pinpoint.testweb.service.ArcusService;
import com.navercorp.pinpoint.testweb.service.MemcachedService;

@Controller
public class MemcachedController {

    @Autowired
    private MemcachedService memcachedService;

    @RequestMapping(value = "/memcached/get")
    @ResponseBody
    public String get() {
        memcachedService.get();
        return "OK";
    }

    @RequestMapping(value = "/memcached/set")
    @ResponseBody
    public String set() {
        memcachedService.set();
        return "OK";
    }

    @RequestMapping(value = "/memcached/delete")
    @ResponseBody
    public String delete() {
        memcachedService.delete();
        return "OK";
    }

    @RequestMapping(value = "/memcached/timeout")
    @ResponseBody
    public String timeout() {
        memcachedService.timeout();
        return "OK";
    }
    
    @RequestMapping(value = "/memcached/asyncCAS")
    @ResponseBody
    public String asyncCAS() {
        memcachedService.asyncCAS();
        return "OK";
    }
    
    @RequestMapping(value = "/memcached/asyncGetBulk")
    @ResponseBody
    public String asyncGetBulk() {
        memcachedService.asyncGetBulk();
        return "OK";
    }
    
    @RequestMapping(value = "/memcached/getAndTouch")
    @ResponseBody
    public String getAndTouch() {
        memcachedService.getAndTouch();
        return "OK";
    }
}
