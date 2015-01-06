package com.navercorp.pinpoint.testweb.connector.apachehttp3;

import java.io.IOException;
import java.util.HashMap;

import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;

public class ApacheHttpClient3 {
       
    public void executeGet(String url, HashMap<String, Object> hashMap, Object object) {
        
        // Create an instance of HttpClient.
        HttpClient client = new HttpClient();

        // Create a method instance.
        GetMethod method = new GetMethod(url);
        
        // Provide custom retry handler is necessary
        method.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, 
                new DefaultHttpMethodRetryHandler(3, false));
        method.setQueryString(new NameValuePair[] { new NameValuePair("key2", "value2") });
        try {
          // Execute the method.
          int statusCode = client.executeMethod(method);
          statusCode = client.executeMethod(null, method);
          statusCode = client.executeMethod(null, method, null);

          if (statusCode != HttpStatus.SC_OK) {
            System.err.println("Method failed: " + method.getStatusLine());
          }

          // Read the response body.
          byte[] responseBody = method.getResponseBody();

          // Deal with the response.
          // Use caution: ensure correct character encoding and is not binary data
//          System.out.println(new String(responseBody));

        } catch (HttpException e) {
          System.err.println("Fatal protocol violation: " + e.getMessage());
          e.printStackTrace();
        } catch (IOException e) {
          System.err.println("Fatal transport error: " + e.getMessage());
          e.printStackTrace();
        } finally {
          // Release the connection.
          method.releaseConnection();
        }  
    }

    public void executePost() {
        String url = "http://www.naver.com";
        
        // Create an instance of HttpClient.
        HttpClient client = new HttpClient();

        // Create a method instance.
        PostMethod method = new PostMethod(url);
        
        // Provide custom retry handler is necessary
        method.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, 
                new DefaultHttpMethodRetryHandler(3, false));
        method.setQueryString(new NameValuePair[] { new NameValuePair("newkey", "value") });
        method.addParameter("key2", "value2");
        try {
          // Execute the method.
          int statusCode = client.executeMethod(method);
          statusCode = client.executeMethod(null, method);
          statusCode = client.executeMethod(null, method, null);

          if (statusCode != HttpStatus.SC_OK) {
            System.err.println("Method failed: " + method.getStatusLine());
          }

          // Read the response body.
          byte[] responseBody = method.getResponseBody();

          // Deal with the response.
          // Use caution: ensure correct character encoding and is not binary data
//          System.out.println(new String(responseBody));

        } catch (HttpException e) {
          System.err.println("Fatal protocol violation: " + e.getMessage());
          e.printStackTrace();
        } catch (IOException e) {
          System.err.println("Fatal transport error: " + e.getMessage());
          e.printStackTrace();
        } finally {
          // Release the connection.
          method.releaseConnection();
        }  
        
    }

    public void executeWithCookie(String url, HashMap<String, Object> hashMap, Object object) {
        
        // Create an instance of HttpClient.
        HttpClient client = new HttpClient();

        // Create a method instance.
        PostMethod method = new PostMethod(url);
        
        // Provide custom retry handler is necessary
        method.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, 
                new DefaultHttpMethodRetryHandler(3, false));
        method.setQueryString(new NameValuePair[] { new NameValuePair("newkey", "value") });
        method.addParameter("key2", "value2");
        method.setRequestHeader("Cookie", "cookieKey1=Cookievalue2");
        
        try {
          // Execute the method.
          int statusCode = client.executeMethod(method);
//          statusCode = client.executeMethod(null, method);
//          statusCode = client.executeMethod(null, method, null);

          if (statusCode != HttpStatus.SC_OK) {
            System.err.println("Method failed: " + method.getStatusLine());
          }

          // Read the response body.
          byte[] responseBody = method.getResponseBody();

          // Deal with the response.
          // Use caution: ensure correct character encoding and is not binary data
//          System.out.println(new String(responseBody));

        } catch (HttpException e) {
          System.err.println("Fatal protocol violation: " + e.getMessage());
          e.printStackTrace();
        } catch (IOException e) {
          System.err.println("Fatal transport error: " + e.getMessage());
          e.printStackTrace();
        } finally {
          // Release the connection.
          method.releaseConnection();
        }  
        
    }
}
