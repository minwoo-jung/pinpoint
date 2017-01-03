/**
 * Copyright 2014 NAVER Corp.
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
 */
package com.navercorp.test.pinpoint.plugin.bloc.v4.module;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;

import org.junit.runner.JUnitCore;
import org.junit.runner.Runner;

import com.navercorp.pinpoint.test.plugin.ForkedPinpointPluginTestRunner;
import com.navercorp.pinpoint.test.plugin.PinpointPluginTestRunListener;
import com.nhncorp.lucy.bloc.annotation.Procedure;
import com.nhncorp.lucy.bloc.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jongho Moon
 *
 */
@Resource(name="test")
public class TestBO {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Procedure
    public String doTest(String testId, String testClass, String testClassPath) throws Exception {

        ClassLoader loader = getClass().getClassLoader();
         
        if (testClassPath != null) {
            URL url = new File(testClassPath).toURI().toURL();
            URL[] urls = new URL[] { url };
            loader = new URLClassLoader(urls, loader);
        }
        
        Runner runner;
        
        Class<?> tc = loader.loadClass(testClass);
        runner = new ForkedPinpointPluginTestRunner(tc, testId);

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        
        JUnitCore junit = new JUnitCore();
        junit.addListener(new PinpointPluginTestRunListener(os));
        
        junit.run(runner);
        
        return new String(os.toByteArray());
    }
}
