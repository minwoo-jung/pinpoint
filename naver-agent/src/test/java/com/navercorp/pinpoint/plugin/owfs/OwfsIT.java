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

package com.navercorp.pinpoint.plugin.owfs;

import static com.navercorp.pinpoint.bootstrap.plugin.test.Expectations.*;
import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifier;
import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifierHolder;
import com.navercorp.pinpoint.common.Version;
import com.navercorp.pinpoint.plugin.NaverAgentPath;
import com.navercorp.pinpoint.test.plugin.Dependency;
import com.navercorp.pinpoint.test.plugin.PinpointAgent;
import com.navercorp.pinpoint.test.plugin.PinpointPluginTestSuite;
import com.navercorp.pinpoint.test.plugin.Repository;
import com.nhncorp.owfs.Owfs;
import com.nhncorp.owfs.OwfsFile;
import com.nhncorp.owfs.OwfsOwner;
import com.nhncorp.owfs.base.Configuration;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.lang.reflect.Method;
import java.net.InetAddress;

/**
 * @author jaehong.kim
 */
@RunWith(PinpointPluginTestSuite.class)
@PinpointAgent(NaverAgentPath.PATH)
@Repository("http://repo.navercorp.com/maven2")
@Dependency({"com.nhncorp.owfs:owfs-api:[3.6.0,4.0.9]", "log4j:log4j:1.2.16", "org.slf4j:slf4j-log4j12:1.7.5", "com.nhncorp.nelo2:nelo2-java-sdk-log4j:1.3.3", "commons-logging:commons-logging:1.1.1"})
public class OwfsIT {

    @Test
    public void doTest() throws Exception {
        Method init = Owfs.class.getMethod("init", InetAddress.class, String.class, Configuration.class);
        Method write = OwfsFile.class.getMethod("write", byte[].class, int.class, int.class);

        writeFile();

        PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();
        verifier.printCache();

        verifier.verifyTrace(event("OWFS", init));
        verifier.verifyTrace(event("OWFS", write));

    }

    private void writeFile() {
        Owfs owfs = null;
        OwfsOwner owner = null;
        try {
            owfs = Owfs.init("10.105.64.187", "pinpoint_testweb");
            owner = new OwfsOwner(owfs, "owner_name");
            owner.open();

            OwfsFile dir = new OwfsFile(owner, "/dir");
            if (!dir.exists()) {
                dir.mkdir();
            }

            OwfsFile file = new OwfsFile(owner, "/dir/greeting.txt");
            if (file.exists()) {
                file.delete();
            }
            file.open(true);
            byte[] buffer = "Hello World".getBytes();
            file.write(buffer, 0, buffer.length);
            file.close();
            owner.close();
            owfs.close();
        } catch (Exception e) {
        }
    }
}
