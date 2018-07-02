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

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.async.DeferredResult;

/**
 * 
 */
@EnableAsync
@Controller
public class SpringController {

    @RequestMapping(value = "/spring/mvc/async/callable")
    @ResponseBody
    public Callable<String> springMvcAsyncCallable() throws Exception {

        Callable<String> callback = new Callable<String>() {
            @Override
            public String call() throws Exception {
                
                TimeUnit.SECONDS.sleep(3);
                
                return "OK";
            }
        };
        return callback;
    }

    @RequestMapping(value = "/spring/mvc/async/deferred")
    @ResponseBody
    public DeferredResult<String> springMvcDeferred() throws Exception {
        final DeferredResult<String> result = new DeferredResult<String>();

        Runnable callback = new Runnable() {
            public void run() {
                try {
                    TimeUnit.SECONDS.sleep(3);
                } catch (InterruptedException ignored) {
                }
                result.setResult("OK");
            }
        };

        new Thread(callback).start();

        return result;
    }

    @RequestMapping(value = "/spring/mvc/async/error")
    @ResponseBody
    public Callable<String> springMvcError() throws Exception {
        Callable<String> callback = new Callable<String>() {
            @Override
            public String call() throws Exception {

                TimeUnit.SECONDS.sleep(3);
                throw new Exception("Failed to asynchronous operation");
            }
        };
        return callback;
    }
}