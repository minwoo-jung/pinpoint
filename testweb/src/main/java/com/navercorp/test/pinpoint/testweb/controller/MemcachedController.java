/*
 * Copyright 2016 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.navercorp.test.pinpoint.testweb.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.navercorp.test.pinpoint.testweb.service.MemcachedService;

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
