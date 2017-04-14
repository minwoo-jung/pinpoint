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
package com.navercorp.pinpoint.plugin.bloc.v3;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Arrays;
import java.util.List;

import com.navercorp.pinpoint.bootstrap.plugin.ApplicationTypeDetector;
import com.navercorp.pinpoint.bootstrap.resolver.ConditionProvider;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.common.util.ArrayUtils;
import com.navercorp.pinpoint.common.util.CollectionUtils;
import com.navercorp.pinpoint.plugin.bloc.BlocConstants;

/**
 * @author Jongho Moon
 * @author HyunGil Jeong
 *
 */
public class Bloc3Detector implements ApplicationTypeDetector {

    private static final String DEFAULT_BOOTSTRAP_MAIN = "org.apache.catalina.startup.Bootstrap";

    private static final String REQUIRED_SYSTEM_PROPERTY = "catalina.home";

    private static final String REQUIRED_CLASS = "org.apache.catalina.startup.Bootstrap";

    private static final String BLOC3_BOOTSTRAP_JAR_PREFIX = "lucy-bloc-bootstrap";

    private final List<String> bootstrapMains;

    public Bloc3Detector(List<String> bootstrapMains) {
        if (CollectionUtils.isEmpty(bootstrapMains)) {
            this.bootstrapMains = Arrays.asList(DEFAULT_BOOTSTRAP_MAIN);
        } else {
            this.bootstrapMains = bootstrapMains;
        }
    }

    @Override
    public ServiceType getApplicationType() {
        return BlocConstants.BLOC;
    }

    @Override
    public boolean detect(ConditionProvider provider) {
        if (provider.checkMainClass(bootstrapMains) &&
            provider.checkForClass(REQUIRED_CLASS)) {
            String catalinaHomePath = provider.getSystemPropertyValue(REQUIRED_SYSTEM_PROPERTY);
            return testForBlocEnvironment(catalinaHomePath);
        }
        return false;
    }

    private boolean testForBlocEnvironment(String catalinaHome) {
        File bloc3CatalinaJar = new File(catalinaHome + "/server/lib/catalina.jar");
        File bloc3ServletApiJar = new File(catalinaHome + "/common/lib/servlet-api.jar");
        File bloc3Directory = new File(catalinaHome + "/bloc/server/lib");
        boolean bloc3BootstrapJarExists = testForBloc3BootstrapJar(bloc3Directory);
        if (bloc3CatalinaJar.exists() && bloc3ServletApiJar.exists() && bloc3BootstrapJarExists) {
            return true;
        }
        return false;
    }

    private boolean testForBloc3BootstrapJar(File bloc3Directory) {
        File[] files = bloc3Directory.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                if (name == null) {
                    return false;
                }
                if (name.startsWith(BLOC3_BOOTSTRAP_JAR_PREFIX) && name.endsWith(".jar")) {
                    return true;
                }
                return false;
            }
        });

        if (ArrayUtils.isEmpty(files)) {
            return false;
        } else {
            return true;
        }
    }

}
