package com.navercorp.pinpoint.testweb.controller;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.api.client.extensions.appengine.http.UrlFetchTransport;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.apache.ApacheHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;

@Controller
public class GoogleHttpClientController {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private static final HttpTransport NET_HTTP_TRANSPORT = new NetHttpTransport();
    private static final HttpTransport APACHE_HTTP_TRANSPORT = new ApacheHttpTransport();
    private static final HttpTransport URL_FETCH_TRANSPORT = new UrlFetchTransport();

    @RequestMapping(value = "/google/httpclient/nethttp/execute")
    @ResponseBody
    public String netHttpExcecute() {
        HttpRequestFactory requestFactory = NET_HTTP_TRANSPORT.createRequestFactory(new HttpRequestInitializer() {
            @Override
            public void initialize(HttpRequest request) {
            }
        });
        
        GenericUrl url = new GenericUrl("http://naver.com");
        HttpRequest request;
        try {
            request = requestFactory.buildGetRequest(url);
            HttpResponse response = request.execute();
            response.parseAsString();
        } catch (IOException e) {
            return e.getMessage();
        }

        return "OK";
    }
    
    @RequestMapping(value = "/google/httpclient/apachehttp/execute")
    @ResponseBody
    public String apacheHttpExecute() {
        HttpRequestFactory requestFactory = APACHE_HTTP_TRANSPORT.createRequestFactory(new HttpRequestInitializer() {
            @Override
            public void initialize(HttpRequest request) {
            }
        });
        
        String result = null;
        GenericUrl url = new GenericUrl("http://naver.com");
        HttpRequest request;
        try {
            request = requestFactory.buildGetRequest(url);
            HttpResponse response = request.execute();
            response.parseAsString();
        } catch (IOException e) {
            return e.getMessage();
        }

        return "OK";
    }
    
    @RequestMapping(value = "/google/httpclient/apachehttp/executeAsync")
    @ResponseBody
    public String urlFetchExecute() {
        HttpRequestFactory requestFactory = APACHE_HTTP_TRANSPORT.createRequestFactory(new HttpRequestInitializer() {
            @Override
            public void initialize(HttpRequest request) {
            }
        });
        
        String result = null;
        GenericUrl url = new GenericUrl("http://naver.com");
        HttpRequest request;
        try {
            request = requestFactory.buildGetRequest(url);
            Future<HttpResponse> future = request.executeAsync();
            future.get().parseAsString();
        } catch (Exception e) {
            return e.getMessage();
        }

        return "OK";
    }
}