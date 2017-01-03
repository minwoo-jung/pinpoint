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
package com.navercorp.test.pinpoint.plugin.bloc.v4;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import com.navercorp.pinpoint.test.plugin.PinpointPluginTestContext;
import com.navercorp.pinpoint.test.plugin.PinpointPluginTestInstance;
import com.navercorp.pinpoint.test.plugin.StreamRedirector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jongho Moon
 *
 */
public class Bloc4PluginTestCase implements PinpointPluginTestInstance {
    private static final String ENCODING = "UTF-8";
    
    private final PinpointPluginTestContext context;
    private final File blocDir;
    private final String testId;
    
    private final File blocBase = new File("test/bloc4/base");
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
 
    public Bloc4PluginTestCase(PinpointPluginTestContext context, File blocDir) {
        this.context = context;
        this.blocDir = blocDir;
        this.testId = blocDir.getName() + ":" + context.getJvmVersion();
    }

    @Override
    public String getTestId() {
        return testId;
    }

    @Override
    public List<String> getClassPath() {
        List<String> classpath = new ArrayList<>();
        
        File lib = new File(blocDir, "libs");
        
        for (File child : lib.listFiles()) {
            if (child.getName().endsWith(".jar")) {
                classpath.add(child.getAbsolutePath());
            }
        }
        
        File conf = new File(blocBase, "conf");
        classpath.add(conf.getAbsolutePath());
        
        return classpath;
    }

    @Override
    public List<String> getVmArgs() {
        return Arrays.asList("-Dbloc.home=" + blocDir.getAbsolutePath(),
                "-Dbloc.base=" + blocBase.getAbsolutePath(),
                "-Djava.io.tmpdir=" + new File(blocBase, "temp").getAbsolutePath());
    }

    @Override
    public String getMainClass() {
        return "com.nhncorp.lucy.bloc.server.BlocServer";
    }

    @Override
    public List<String> getAppArgs() {
        File conf = new File(blocBase, "conf");
        File ini = new File(conf, "bloc.ini");
        
        return Arrays.asList(ini.getAbsolutePath());
    }
    
     @Override
    public File getWorkingDirectory() {
        return blocBase;
    }

    @Override
    public Scanner startTest(Process process) throws Throwable {
        new Thread(new StreamRedirector(process.getInputStream(), System.out)).start();

        String testClass = context.getTestClass().getName();
        String testClassLocation = context.getTestClassLocation();
        
        String urlString = "http://localhost:5098/test/test/doTest?testId=" + testId + "&testClass=" + testClass + "&testClassPath=" + testClassLocation;
        logger.debug("Try to call: " + urlString);
        
        URL url = new URL(urlString);

        for (int i = 0; i < 10; i++) {
            
            HttpURLConnection connection;
            try {
                connection = (HttpURLConnection)url.openConnection();
                connection.connect();

                int response = connection.getResponseCode();
                logger.debug("response: " + response);
                
//                if (response != HttpURLConnection.HTTP_OK) {
//                    throw new RuntimeException("Failed to invoke " + url + " [" + response + "]");
//                }
            } catch (IOException e) {
                // connection failed. retry.
                Thread.sleep(1000);
                
                logger.debug("Retry " + (i + 1) + "th time to call test servlet");
                continue;
            }
            
            InputStream is = connection.getInputStream();
            String encoding = connection.getContentEncoding();
            Scanner scanner = new Scanner(is, encoding == null ? ENCODING : encoding);
            String response = scanner.nextLine();
            scanner.close();
            
            String modified = response.substring(1, response.length() - 1).replace("\\n", "\n");
            
            return new Scanner(modified);
        }
        
        throw new RuntimeException("Failed to connect BLOC");
    }

    @Override
    public void endTest(Process process) throws Throwable {
        final Socket socket = new Socket();
        PrintWriter writer = null;
        try {
            socket.connect(new InetSocketAddress("127.0.0.1", 9984));
            writer = new PrintWriter(socket.getOutputStream());
            writer.write("STOP!");
        } catch (IOException e) {
            logger.error("Fail to send shutdown message", e);
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (Exception ignore) {
                }
            }
            try {
                socket.close();
            } catch (IOException e) {
                logger.error("Fail to close socket", e);
            }
        }
    }
}
