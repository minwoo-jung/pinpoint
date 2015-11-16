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
import java.util.ArrayList;
import java.util.List;

import org.eclipse.aether.resolution.ArtifactResolutionException;
import org.eclipse.aether.resolution.DependencyResolutionException;
import org.junit.runners.model.InitializationError;

import com.navercorp.pinpoint.test.plugin.AbstractPinpointPluginTestSuite;
import com.navercorp.pinpoint.test.plugin.PinpointPluginTestInstance;
import com.navercorp.pinpoint.test.plugin.PinpointPluginTestContext;

/**
 * @author Jongho Moon
 *
 */
public class Bloc4PluginTestSuite extends AbstractPinpointPluginTestSuite {

    public Bloc4PluginTestSuite(Class<?> testClass) throws InitializationError, ArtifactResolutionException, DependencyResolutionException {
        super(testClass);
    }

    
    @Override
    protected List<PinpointPluginTestInstance> createTestCases(PinpointPluginTestContext context) {
        List<PinpointPluginTestInstance> runners = new ArrayList<PinpointPluginTestInstance>();
        
        File file = new File("test/bloc4/releases");
        
        if (!file.exists()) {
            throw new RuntimeException("Cannot find bloc releses directory: " + file.getAbsolutePath());
        }
        
        for (File child : file.listFiles()) {
            if (!child.isDirectory()) {
                continue;
            }
            
            Bloc4PluginTestCase runner = new Bloc4PluginTestCase(context, child);
            runners.add(runner);
        }

        return runners;
    }
}
