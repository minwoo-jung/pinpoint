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
public class SpringWebController {

    @RequestMapping(value = "/spring/mvc/async/callable")
    @ResponseBody
    public Callable<String> springMvcAsyncCallable() throws Exception {

        Callable<String> callback = new Callable<String>() {
            @Override
            public String call() throws Exception {
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < 3; i++) {
                    TimeUnit.SECONDS.sleep(1);
                    sb.append(" ").append(i);
                }

                return sb.toString();
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
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < 3; i++) {
                    try {
                        TimeUnit.SECONDS.sleep(1);
                    } catch (InterruptedException e) {
                    }
                    sb.append(" ").append(i);
                }
                result.setResult(sb.toString());
            }
        };

        new Thread(callback).run();

        try {
            TimeUnit.SECONDS.sleep(5);
        } catch (InterruptedException e) {
        }

        return result;
    }
}