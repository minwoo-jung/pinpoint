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
package com.navercorp.pinpoint.plugin.tomcat;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import com.navercorp.pinpoint.test.plugin.DefaultProcessManager;
import com.navercorp.pinpoint.test.plugin.PinpointPluginTestInstance;
import com.navercorp.pinpoint.test.plugin.PinpointPluginTestContext;
import com.navercorp.pinpoint.test.plugin.ProcessManager;
import com.navercorp.pinpoint.test.plugin.StreamRedirector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jongho Moon
 *
 */
public class TomcatPluginTestCase implements PinpointPluginTestInstance {
    private static final String ENCODING = "UTF-8";

    private final PinpointPluginTestContext context;
    private final File tomcatHome;
    private final String testId;
    private final File tomcatBase = new File("test/tomcat/base");
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final ProcessManager processManager;

    public TomcatPluginTestCase(PinpointPluginTestContext context, File tomcatDir) {
        this.context = context;
        this.tomcatHome = tomcatDir;
        this.testId = tomcatDir.getName() + ":" + context.getJvmVersion();
        this.processManager = new DefaultProcessManager(context);
    }

    @Override
    public String getTestId() {
        return testId;
    }

    @Override
    public List<String> getClassPath() {
        List<String> libs = new ArrayList<String>();

        File bin = new File(tomcatHome, "bin");
        libs.add(new File(bin, "bootstrap.jar").getAbsolutePath());
        libs.add(new File(bin, "tomcat-juli.jar").getAbsolutePath());

        return libs;
    }

    @Override
    public List<String> getVmArgs() {
        return Arrays.asList("-Dcatalina.home=" + tomcatHome.getAbsolutePath(),
                "-Dcatalina.base=" + tomcatBase.getAbsolutePath(),
                "-Djava.endorsed.dirs=" + new File(tomcatHome, "endorsed").getAbsolutePath(),
                "-Dfile.encoding=UTF-8");
    }

    @Override
    public String getMainClass() {
        return "org.apache.catalina.startup.Bootstrap";
    }

    @Override
    public List<String> getAppArgs() {
        return Arrays.asList("start");
    }

    @Override
    public File getWorkingDirectory() {
        return tomcatBase;
    }

    @Override
    public Scanner startTest() throws Throwable {
        Process process = processManager.create(this);
        new Thread(new StreamRedirector(process.getInputStream(), System.out)).start();

        String testClass = context.getTestClass().getName();
        String testClassLocation = context.getTestClassLocation();

        String urlString = "http://localhost:8972/test/doTest?testId=" + testId + "&testClass=" + testClass + "&testClassPath=" + testClassLocation;
        logger.debug("Try to call: " + urlString);

        URL url = new URL(urlString);

        for (int i = 0; i < 10; i++) {

            HttpURLConnection connection;
            try {
                connection = (HttpURLConnection)url.openConnection();
                connection.connect();

                int response = connection.getResponseCode();
                logger.debug("response: " + response);

                if (response != HttpURLConnection.HTTP_OK) {
                    throw new RuntimeException("Failed to invoke " + url + " [" + response + "]");
                }
            } catch (IOException e) {
                // connection failed. retry.
                Thread.sleep(1000);

                logger.debug("Retry " + (i + 1) + "th time to call test servlet");
                continue;
            }

            InputStream is = connection.getInputStream();

            String encoding = connection.getContentEncoding();
            return new Scanner(is, encoding == null ? ENCODING : encoding);
        }

        throw new RuntimeException("Failed to connect tomcat");
    }

    @Override
    public void endTest() throws Throwable {
        try {
            List<String> command = new ArrayList<String>();

            command.add(context.getJavaExecutable());
            command.add("-cp");

            StringBuilder classPath = new StringBuilder();

            for (String lib : getClassPath()) {
                classPath.append(lib);
                classPath.append(File.pathSeparatorChar);
            }

            command.add(classPath.toString());
            command.addAll(getVmArgs());
            command.add(getMainClass());
            command.add("stop");

            ProcessBuilder builder = new ProcessBuilder(command);
            builder.redirectErrorStream(true);

            Process stopProcess = builder.start();
            new Thread(new StreamRedirector(stopProcess.getInputStream(), System.out)).start();

            stopProcess.waitFor();
        } finally {
            processManager.stop();
        }
    }
}
