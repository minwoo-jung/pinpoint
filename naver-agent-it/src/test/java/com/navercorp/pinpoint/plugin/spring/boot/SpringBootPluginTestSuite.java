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

package com.navercorp.pinpoint.plugin.spring.boot;

import com.navercorp.pinpoint.test.plugin.PinpointPluginTestContext;
import com.navercorp.pinpoint.test.plugin.PinpointPluginTestInstance;
import com.navercorp.pinpoint.test.plugin.PinpointPluginTestSuite;
import org.eclipse.aether.resolution.ArtifactResolutionException;
import org.eclipse.aether.resolution.DependencyResolutionException;
import org.junit.runners.model.InitializationError;

import java.util.ArrayList;
import java.util.List;

/**
 * @author HyunGil Jeong
 */
public abstract class SpringBootPluginTestSuite extends PinpointPluginTestSuite {

    protected final TestAppVersion version;

    public SpringBootPluginTestSuite(Class<?> testClass) throws InitializationError, ArtifactResolutionException, DependencyResolutionException {
        super(testClass, false);
        TestAppVersion version = testClass.getAnnotation(TestAppVersion.class);
        if (version == null) {
            throw new IllegalArgumentException("@TestAppVersion must be specified");
        }
        this.version = version;
    }

    @Override
    protected boolean usingSharedProcess() {
        return false;
    }

    @Override
    protected List<PinpointPluginTestInstance> createTestCases(PinpointPluginTestContext context) throws Exception {
        List<PinpointPluginTestInstance> runners = super.createTestCases(context);
        List<PinpointPluginTestInstance> delegatedRunners = new ArrayList<PinpointPluginTestInstance>(runners.size());

        for (PinpointPluginTestInstance runner : runners) {
            delegatedRunners.add(createRunner(context, runner));
        }

        return delegatedRunners;
    }

    protected abstract SpringBootPluginTestCase createRunner(PinpointPluginTestContext context, PinpointPluginTestInstance delegate);

    public static class SpringBootPluginJarLauncherTestSuite extends SpringBootPluginTestSuite {

        public SpringBootPluginJarLauncherTestSuite(Class<?> testClass) throws InitializationError, ArtifactResolutionException, DependencyResolutionException {
            super(testClass);
        }

        @Override
        protected SpringBootPluginTestCase createRunner(PinpointPluginTestContext context, PinpointPluginTestInstance delegate) {
            return new SpringBootPluginTestCase(context, delegate, this.version.value(), LauncherType.JAR);
        }
    }

    public static class SpringBootPluginWarLauncherTestSuite extends SpringBootPluginTestSuite {

        public SpringBootPluginWarLauncherTestSuite(Class<?> testClass) throws InitializationError, ArtifactResolutionException, DependencyResolutionException {
            super(testClass);
        }

        @Override
        protected SpringBootPluginTestCase createRunner(PinpointPluginTestContext context, PinpointPluginTestInstance delegate) {
            return new SpringBootPluginTestCase(context, delegate, this.version.value(), LauncherType.WAR);
        }
    }

    public static class SpringBootPluginPropertiesLauncherTestSuite extends SpringBootPluginTestSuite {

        public SpringBootPluginPropertiesLauncherTestSuite(Class<?> testClass) throws InitializationError, ArtifactResolutionException, DependencyResolutionException {
            super(testClass);
        }

        @Override
        protected SpringBootPluginTestCase createRunner(PinpointPluginTestContext context, PinpointPluginTestInstance delegate) {
            return new SpringBootPluginTestCase(context, delegate, this.version.value(), LauncherType.PROPERTIES);
        }
    }
}
