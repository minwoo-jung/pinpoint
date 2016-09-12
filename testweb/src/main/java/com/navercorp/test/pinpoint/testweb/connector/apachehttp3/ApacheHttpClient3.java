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

package com.navercorp.test.pinpoint.testweb.connector.apachehttp3;

import java.io.IOException;
import java.util.HashMap;

import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ApacheHttpClient3 {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    
    public void executeGet(String url, HashMap<String, Object> hashMap, Object object) {
        
        // Create an instance of HttpClient. Create a method instance.
        HttpClient client = new HttpClient();
        GetMethod method = new GetMethod(url);
        
        // Provide custom retry handler is necessary
        method.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler(3, false));
        method.setQueryString(new NameValuePair[] { new NameValuePair("key2", "value2") });
        
        try {
          // Execute the method.
          int statusCode = client.executeMethod(method);

          if (statusCode != HttpStatus.SC_OK) {
              logger.error("Method failed: " + method.getStatusLine());
          }

        } catch (HttpException e) {
            logger.error("Fatal protocol violation: " + e.getMessage(), e);
        } catch (IOException e) {
            logger.error("Fatal transport error: " + e.getMessage(), e);
        } finally {
          method.releaseConnection();
        }  
    }

    public void executePost() {
        String url = "http://www.naver.com";
        
        // Create an instance of HttpClient. Create a method instance.
        HttpClient client = new HttpClient();
        PostMethod method = new PostMethod(url);
        
        // Provide custom retry handler is necessary
        method.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler(3, false));
        method.setQueryString(new NameValuePair[] { new NameValuePair("newkey", "value") });
        method.addParameter("key2", "value2");
        
        try {
          // Execute the method.
          int statusCode = client.executeMethod(method);

          if (statusCode != HttpStatus.SC_OK) {
              logger.error("Method failed: " + method.getStatusLine());
          }

        } catch (HttpException e) {
            logger.error("Fatal protocol violation: " + e.getMessage(), e);
        } catch (IOException e) {
            logger.error("Fatal transport error: " + e.getMessage(), e);
        } finally {
          // Release the connection.
          method.releaseConnection();
        }  
    }

    public void executeWithCookie(String url, HashMap<String, Object> hashMap, Object object) {
        // Create an instance of HttpClient. Create a method instance.
        HttpClient client = new HttpClient();
        PostMethod method = new PostMethod(url);
        
        // Provide custom retry handler is necessary
        method.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler(3, false));
        method.setQueryString(new NameValuePair[] { new NameValuePair("newkey", "value") });
        method.addParameter("key2", "value2");
        method.setRequestHeader("Cookie", "cookieKey1=Cookievalue2");
        
        try {
          int statusCode = client.executeMethod(method);

          if (statusCode != HttpStatus.SC_OK) {
            logger.error("Method failed: " + method.getStatusLine());
          }
        } catch (HttpException e) {
            logger.error("Fatal protocol violation: " + e.getMessage());
        } catch (IOException e) {
            logger.error("Fatal transport error: " + e.getMessage());
        } finally {
          method.releaseConnection();
        }  
        
    }

    public void executeDefaultConfig() {
        // Create an instance of HttpClient. Create a method instance.
        HttpClient client = new HttpClient();

        HostConfiguration config = new HostConfiguration();
        config.setHost("weather.naver.com", 80, "http");
        GetMethod method2 = new GetMethod("/rgn/cityWetrMain.nhn");
        method2.setQueryString(new NameValuePair[] { new NameValuePair("newkey", "value") });

        try {
            // Execute the method.
            client.executeMethod(config, method2);
        } catch (HttpException e) {
            logger.error("Fatal protocol violation: " + e.getMessage(), e);
        } catch (IOException e) {
            logger.error("Fatal transport error: " + e.getMessage(), e);
        } finally {
            method2.releaseConnection();
        }
    }
}