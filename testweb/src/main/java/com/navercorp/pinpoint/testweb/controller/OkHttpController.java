package com.navercorp.pinpoint.testweb.controller;

import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Created by nbp on 2015-09-07.
 */

@Controller
public class OkHttpController {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @RequestMapping(value="/okhttp/call/execute")
    @ResponseBody
    public String execute() {
        logger.info("execute");

        Request request = new Request.Builder().url("http://google.com").build();
        OkHttpClient client = new OkHttpClient();
        try {
            Response response = client.newCall(request).execute();
            logger.info("Response {}", response.body().string());
        } catch (IOException e) {
            logger.error("Unable to execute", e);
        }

        return "Ok";
    }

    @RequestMapping(value="/okhttp/call/enqueue")
    @ResponseBody
    public String enqueue() {
        logger.info("enqueue");

        Request request = new Request.Builder().url("http://google.com").build();
        OkHttpClient client = new OkHttpClient();
        final CountDownLatch latch = new CountDownLatch(1);
        try {
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Request request, IOException e) {
                    logger.info("Failed to enqueue", e);
                    latch.countDown();
                }

                @Override
                public void onResponse(Response response) throws IOException {
                    logger.info("Response {}", response.body().string());
                    latch.countDown();
                }
            });
            latch.await(3, TimeUnit.SECONDS);
        } catch (Exception e) {
            logger.error("Unable to execute", e);
        }

        return "Ok";
    }

    @RequestMapping(value="/okhttp/call/enqueueAndCallback")
    @ResponseBody
    public String enqueueAndCallback() {
        logger.info("enqueue");

        Request request = new Request.Builder().url("http://google.com").build();
        OkHttpClient client = new OkHttpClient();
        final CountDownLatch latch = new CountDownLatch(1);
        try {
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Request request, IOException e) {
                    logger.info("Failed to enqueue", e);
                    latch.countDown();
                }

                @Override
                public void onResponse(Response response) throws IOException {
                    logger.info("Response {}", response.body().string());

                    Request request = new Request.Builder().url("http://google.com").build();
                    OkHttpClient client = new OkHttpClient();
                    try {
                        Response r = client.newCall(request).execute();
                        logger.info("Response {}", r.body().string());
                    } catch (IOException e) {
                        logger.error("Unable to execute", e);
                    }
                    latch.countDown();
                }
            });
            latch.await(3, TimeUnit.SECONDS);
        } catch (Exception e) {
            logger.error("Unable to execute", e);
        }

        return "Ok";
    }


    @RequestMapping(value="/okhttp/call/failed")
    @ResponseBody
    public String failed() {
        logger.info("failed");

        Request request = new Request.Builder().url("http://127.0.0.1:9999").build();
        OkHttpClient client = new OkHttpClient();
        try {
            Response response = client.newCall(request).execute();
            logger.info("Response {}", response.body().string());
        } catch (IOException e) {
            logger.error("Unable to execute", e);
        }

        return "Ok";
    }

    @RequestMapping(value="/okhttp/call/localRequest")
    @ResponseBody
    public String local(final HttpServletRequest servletRequest) {
        logger.info("localRequest");

        Request request = new Request.Builder().url("http://" + servletRequest.getLocalAddr() + ":" + servletRequest.getLocalPort()).build();
        OkHttpClient client = new OkHttpClient();
        try {
            Response response = client.newCall(request).execute();
            logger.info("Response {}", response.body().string());
        } catch (IOException e) {
            logger.error("Unable to execute", e);
        }

        return "Ok";
    }


}
