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
 */

package com.navercorp.pinpoint.plugin.spring.boot.it.props;

import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifier;
import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifierHolder;
import com.navercorp.pinpoint.common.Version;
import com.navercorp.pinpoint.plugin.spring.boot.SpringBootPluginTestSuite;
import com.navercorp.pinpoint.plugin.spring.boot.TestAppSpringBootVersion;
import com.navercorp.pinpoint.plugin.spring.boot.TestAppVersion;
import com.navercorp.pinpoint.test.plugin.Dependency;
import com.navercorp.pinpoint.test.plugin.JvmVersion;
import com.navercorp.pinpoint.test.plugin.OnClassLoader;
import com.navercorp.pinpoint.test.plugin.PinpointAgent;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

/**
 * @author HyunGil Jeong
 */
@RunWith(SpringBootPluginTestSuite.SpringBootPluginPropertiesLauncherTestSuite.class)
@PinpointAgent("naver-agent/target/pinpoint-naver-agent-" + Version.VERSION)
@JvmVersion(7)
@OnClassLoader(system = true, child = false)
@Dependency({ "org.springframework.boot:spring-boot-loader:[1.0.0.RELEASE,1.0.max]" })
@TestAppVersion(TestAppSpringBootVersion.PRE_1_4)
public class SpringBootPropertiesLauncher_1_0_X_IT extends PropertiesLauncherItBase {

    @Test
    public void testBootstrap() {
        PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();
        verifier.verifyServerType(SERVER_TYPE);
        // 1.0.x includes nested directories - https://github.com/spring-projects/spring-boot/issues/1352
        List<String> expectedLibs = new ExpectedLibraries()
                .withExecutable()
                .withAgentJar()
                .withClasspathLib()
                .withPackagedLib()
                .withCustomEntry(getExecutable() + NESTED_LIB_SEPARATOR + getEntryPath())
                .getLibraries();
        verifier.verifyService(EXPECTED_CONTEXT, expectedLibs);
    }
}
