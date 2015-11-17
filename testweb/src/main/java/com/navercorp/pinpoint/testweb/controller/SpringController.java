package com.navercorp.pinpoint.testweb.controller;

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
}