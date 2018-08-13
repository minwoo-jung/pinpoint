/*
 * Copyright 2018 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.navercorp.pinpoint.plugin.tomcat.webapp;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.runner.JUnitCore;
import org.junit.runner.Runner;

import com.navercorp.pinpoint.test.plugin.ForkedPinpointPluginTestRunner;
import com.navercorp.pinpoint.test.plugin.PinpointPluginTestRunListener;

/**
 * @author Jongho Moon
 *
 */
@SuppressWarnings("serial")
public class TestServlet extends HttpServlet {
    private static HttpServletRequest request;
    
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String testId = req.getParameter("testId");
        String testClassName = req.getParameter("testClass");
        String[] testClassPath = req.getParameterValues("testClassPath");

        ClassLoader loader = getClass().getClassLoader();
        
        if (testClassPath != null) {
            URL[] urls = new URL[testClassPath.length];
            
            for (int i = 0; i < testClassPath.length; i++) {
                urls[i] = new File(testClassPath[i]).toURI().toURL();
            }
            
            loader = new URLClassLoader(urls, loader);
        }
        
        Runner runner;
        
        try {
            Class<?> testClass = loader.loadClass(testClassName);
            runner = new ForkedPinpointPluginTestRunner(testClass, testId);
        } catch (Exception e) {
            throw new ServletException(e);
        }

        resp.setStatus(HttpServletResponse.SC_OK);

        JUnitCore junit = new JUnitCore();
        junit.addListener(new PinpointPluginTestRunListener(resp.getOutputStream()));
        
        request = req;

        try {
            junit.run(runner);
            resp.flushBuffer();
        } catch (Throwable t) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            PrintWriter writer = new PrintWriter(resp.getOutputStream());
            writer.println(t.getMessage());
            t.printStackTrace(writer);
        }
    }

    public static HttpServletRequest getRequest() {
        return request;
    }
}
