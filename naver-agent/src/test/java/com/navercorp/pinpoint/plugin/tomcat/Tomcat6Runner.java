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
package com.navercorp.pinpoint.plugin.tomcat;

import org.apache.catalina.Context;
import org.apache.catalina.Engine;
import org.apache.catalina.Host;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.startup.Embedded;

/**
 * @author Jongho Moon
 *
 */
public class Tomcat6Runner {
    private final String catalinaHome;
    private final String appBase;
    private Embedded embedded = null;
    private Host host = null;
    
    public Tomcat6Runner(String catalinaHome, String appBase) {
        this.catalinaHome = catalinaHome;
        this.appBase = appBase;
    }

    public void start(String webapp, int port) throws Exception {
      embedded = new Embedded();
      
      // Create an engine
      Engine engine = embedded.createEngine();
      engine.setDefaultHost("localhost");

      // Create a default virtual host
      host = embedded.createHost("localhost", appBase);
      engine.addChild(host);

      // Create the ROOT context
      Context context = embedded.createContext("", webapp);
      host.addChild(context);

      // Install the assembled container hierarchy
      embedded.addEngine(engine);

      // Assemble and install a default HTTP connector
      Connector connector = embedded.createConnector("localhost", port, false);
      embedded.addConnector(connector);
      // Start the embedded server
      embedded.start();
    }

    /**
      * This method Stops the Tomcat server.
      */
    public void stop() throws Exception {
      // Stop the embedded server
      embedded.stop();
    }
}
