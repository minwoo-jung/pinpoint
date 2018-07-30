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
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import com.navercorp.test.pinpoint.testweb.service.SpringService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

    @Autowired
    private SpringService service;

    @RequestMapping(value = "/spring/mvc/async/callable")
    @ResponseBody
    public Callable<String> asyncCallable() throws Exception {

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
    public DeferredResult<String> asyncDeferred() throws Exception {
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

    @RequestMapping(value = "/spring/mvc/async/timeout")
    @ResponseBody
    public DeferredResult<String> asyncTimeout() throws Exception {
        final DeferredResult<String> result = new DeferredResult<String>(1000L, "TIMEOUT");

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
    public Callable<String> asyncError() throws Exception {
        Callable<String> callback = new Callable<String>() {
            @Override
            public String call() throws Exception {

                TimeUnit.SECONDS.sleep(1);
                throw new Exception("Failed to asynchronous operation");
            }
        };
        return callback;
    }

    @RequestMapping(value = "/spring/mvc/async/500")
    @ResponseBody
    public Callable<ResponseEntity> asyncReturn500() throws Exception {
        Callable<ResponseEntity> callback = new Callable<ResponseEntity>() {
            @Override
            public ResponseEntity call() throws Exception {

                TimeUnit.SECONDS.sleep(1);
                return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
            }
        };
        return callback;
    }

    @RequestMapping(value = "/spring/excute/async/void")
    @ResponseBody
    public String asyncWidthVoid() {
        final long startedTimeMillis = System.currentTimeMillis();
        this.service.asyncWithVoid();
        final long elapsedTimeMillis = System.currentTimeMillis() - startedTimeMillis;
        return "OK " + elapsedTimeMillis + "ms";
    }

    @RequestMapping(value = "/spring/excute/async/future")
    @ResponseBody
    public String asyncWidthFuture() throws Exception {
        final long startedTimeMillis = System.currentTimeMillis();
        Future<String> future = this.service.asyncWithFuture();
        final String result = future.get();
        final long elapsedTimeMillis = System.currentTimeMillis() - startedTimeMillis;
        return "OK " + elapsedTimeMillis + "ms, Result=" + result;
    }

    @RequestMapping(value = "/spring/excute/async/configure")
    @ResponseBody
    public String asyncWidthConfiguredExecutor() {
        final long startedTimeMillis = System.currentTimeMillis();
        this.service.asyncWithConfiguredExecutor();
        final long elapsedTimeMillis = System.currentTimeMillis() - startedTimeMillis;
        return "OK " + elapsedTimeMillis + "ms";
    }

}