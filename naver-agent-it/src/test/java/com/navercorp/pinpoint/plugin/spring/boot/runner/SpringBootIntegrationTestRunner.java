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

package com.navercorp.pinpoint.plugin.spring.boot.runner;

import com.navercorp.pinpoint.test.plugin.ForkedPinpointPluginTestRunner;
import com.navercorp.pinpoint.test.plugin.PinpointPluginTestRunListener;
import org.junit.runner.JUnitCore;
import org.junit.runner.Runner;
import org.junit.runners.model.InitializationError;

/**
 * @author HyunGil Jeong
 */
public class SpringBootIntegrationTestRunner {

    private final String testId;
    private final String testClassName;

    public SpringBootIntegrationTestRunner(String testId, String testClassName) {
        this.testId = testId;
        this.testClassName = testClassName;
    }

    public void run() throws InitializationError, ClassNotFoundException {
        Class<?> testClass = getClass().getClassLoader().loadClass(this.testClassName);
        Runner runner = new ForkedPinpointPluginTestRunner(testClass, this.testId);

        JUnitCore junit = new JUnitCore();
        junit.addListener(new PinpointPluginTestRunListener(System.out));
        junit.run(runner);
    }
}
